package com.karmayuva.pointsystem.service;

import com.karmayuva.pointsystem.dto.AccrualImportResultDto;
import com.karmayuva.pointsystem.entity.Child;
import com.karmayuva.pointsystem.exception.BusinessException;
import com.karmayuva.pointsystem.repository.ChildRepository;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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

    public AccrualImportService(ChildRepository childRepository,
                                 AccrualService accrualService,
                                 NameMatchingService nameMatchingService) {
        this.childRepository = childRepository;
        this.accrualService = accrualService;
        this.nameMatchingService = nameMatchingService;
    }

    @Transactional
    public AccrualImportResultDto importSheet(MultipartFile file) {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase(Locale.ROOT) : "";

        List<List<String>> rows;
        try {
            if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
                rows = readExcel(file);
            } else {
                // .csv, .txt, or anything else - treat as delimited text
                rows = readDelimited(file);
            }
        } catch (Exception e) {
            throw new BusinessException("Failed to read file: " + e.getMessage());
        }

        return processRows(rows);
    }

    private List<List<String>> readDelimited(MultipartFile file) throws Exception {
        // Auto-detect comma vs tab delimiter from the header line
        String firstLine;
        try (var reader = new java.io.BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            firstLine = reader.readLine();
        }
        char delimiter = (firstLine != null && firstLine.contains("\t")) ? '\t' : ',';

        CSVParser parser = new CSVParserBuilder().withSeparator(delimiter).build();
        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                .withCSVParser(parser).build()) {
            List<String[]> raw = reader.readAll();
            List<List<String>> rows = new ArrayList<>();
            for (String[] row : raw) {
                rows.add(List.of(row));
            }
            return rows;
        }
    }

    private List<List<String>> readExcel(MultipartFile file) throws Exception {
        List<List<String>> rows = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            for (Row row : sheet) {
                List<String> cells = new ArrayList<>();
                for (Cell cell : row) {
                    cells.add(formatter.formatCellValue(cell));
                }
                rows.add(cells);
            }
        }
        return rows;
    }

    private AccrualImportResultDto processRows(List<List<String>> rows) {
        List<String> errors = new ArrayList<>();
        int created = 0;
        int processed = 0;

        if (rows.isEmpty()) {
            throw new BusinessException("File is empty");
        }

        List<String> header = rows.get(0).stream().map(h -> h == null ? "" : h.trim().toLowerCase(Locale.ROOT)).toList();
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

            if (row.stream().allMatch(cell -> cell == null || cell.isBlank())) {
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

    private String cell(List<String> row, int index) {
        if (index < 0 || index >= row.size() || row.get(index) == null) return "";
        return row.get(index);
    }
}
