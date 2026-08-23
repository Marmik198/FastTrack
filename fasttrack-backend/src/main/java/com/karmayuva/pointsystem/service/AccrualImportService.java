package com.karmayuva.pointsystem.service;

import com.karmayuva.pointsystem.dto.AccrualImportResultDto;
import com.karmayuva.pointsystem.entity.Child;
import com.karmayuva.pointsystem.exception.BusinessException;
import com.karmayuva.pointsystem.repository.ChildRepository;
import com.karmayuva.pointsystem.util.TabularFileReader;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.karmayuva.pointsystem.util.TabularFileReader.cell;
import static com.karmayuva.pointsystem.util.TabularFileReader.normalizedHeader;

/**
 * Accepts the monthly points sheet as CSV, TXT (comma or tab-delimited), or Excel (.xlsx).
 * Expected columns (header row, case-insensitive, any order): month, frequency, and either
 * childId OR childName (childName is resolved via the same fuzzy matcher the public form
 * uses, so minor spelling differences in the sheet don't cause a hard failure).
 */
@Service
public class AccrualImportService {

    private final ChildRepository childRepository;
    private final AccrualService accrualService;
    private final NameMatchingService nameMatchingService;
    private final TabularFileReader tabularFileReader;

    public AccrualImportService(ChildRepository childRepository,
                                 AccrualService accrualService,
                                 NameMatchingService nameMatchingService,
                                 TabularFileReader tabularFileReader) {
        this.childRepository = childRepository;
        this.accrualService = accrualService;
        this.nameMatchingService = nameMatchingService;
        this.tabularFileReader = tabularFileReader;
    }

    @Transactional
    public AccrualImportResultDto importSheet(MultipartFile file) {
        List<List<String>> rows;
        try {
            rows = tabularFileReader.read(file);
        } catch (Exception e) {
            throw new BusinessException("Failed to read file: " + e.getMessage());
        }
        return processRows(rows);
    }

    private AccrualImportResultDto processRows(List<List<String>> rows) {
        List<String> errors = new ArrayList<>();
        int created = 0;
        int processed = 0;

        if (rows.isEmpty()) {
            throw new BusinessException("File is empty");
        }

        List<String> header = normalizedHeader(rows.get(0));
        int childIdCol = header.indexOf("childid");
        int childNameCol = header.indexOf("childname");
        int monthCol = header.indexOf("month");
        int frequencyCol = header.indexOf("frequency");

        if (monthCol == -1 || frequencyCol == -1 || (childIdCol == -1 && childNameCol == -1)) {
            throw new BusinessException(
                    "File must have 'month', 'frequency', and either 'childId' or 'childName' columns in the header row");
        }

        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            int rowNum = i + 1;

            if (row.stream().allMatch(c -> c == null || c.isBlank())) {
                continue; // skip blank rows
            }

            try {
                String month = cell(row, monthCol);
                String frequencyStr = cell(row, frequencyCol);
                if (month.isBlank() || frequencyStr.isBlank()) {
                    errors.add("Row " + rowNum + ": missing month or frequency");
                    continue;
                }
                int frequency = Integer.parseInt(frequencyStr.trim());

                Child child = resolveChild(row, childIdCol, childNameCol);
                if (child == null) {
                    errors.add("Row " + rowNum + ": could not find a matching child");
                    continue;
                }

                accrualService.createAccrualAndPoint(child, month.trim(), frequency);
                created++;
                processed++;
            } catch (NumberFormatException e) {
                errors.add("Row " + rowNum + ": invalid frequency value");
            } catch (Exception e) {
                errors.add("Row " + rowNum + ": " + e.getMessage());
            }
        }

        return new AccrualImportResultDto(processed, created, errors);
    }

    private Child resolveChild(List<String> row, int childIdCol, int childNameCol) {
        if (childIdCol != -1) {
            String idStr = cell(row, childIdCol);
            if (!idStr.isBlank()) {
                Optional<Child> byId = childRepository.findById(Long.parseLong(idStr.trim()));
                if (byId.isPresent()) return byId.get();
            }
        }
        if (childNameCol != -1) {
            String name = cell(row, childNameCol);
            if (!name.isBlank()) {
                return nameMatchingService.findBestMatch(name).orElse(null);
            }
        }
        return null;
    }
}
