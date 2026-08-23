package com.karmayuva.pointsystem.service;

import com.karmayuva.pointsystem.dto.ChildImportResultDto;
import com.karmayuva.pointsystem.dto.ChildUpsertRequest;
import com.karmayuva.pointsystem.entity.Child;
import com.karmayuva.pointsystem.exception.BusinessException;
import com.karmayuva.pointsystem.repository.ChildRepository;
import com.karmayuva.pointsystem.util.TabularFileReader;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.karmayuva.pointsystem.util.TabularFileReader.cell;
import static com.karmayuva.pointsystem.util.TabularFileReader.normalizedHeader;

/**
 * Bulk-adds children from a CSV, TXT, or Excel (.xlsx) file - the master-data equivalent
 * of AccrualImportService. Expected header columns (case-insensitive, any order):
 * firstName, lastName, mobile are required; motherName, fatherName, alternateMobile,
 * email, age, dateOfBirth (YYYY-MM-DD), address are optional and may be omitted entirely.
 *
 * Row numbers in error messages match the actual spreadsheet row (header = row 1).
 */
@Service
public class ChildImportService {

    private final ChildRepository childRepository;
    private final ChildService childService;
    private final TabularFileReader tabularFileReader;

    public ChildImportService(ChildRepository childRepository, ChildService childService, TabularFileReader tabularFileReader) {
        this.childRepository = childRepository;
        this.childService = childService;
        this.tabularFileReader = tabularFileReader;
    }

    @Transactional
    public ChildImportResultDto importSheet(MultipartFile file) {
        List<List<String>> rows;
        try {
            rows = tabularFileReader.read(file);
        } catch (Exception e) {
            throw new BusinessException("Failed to read file: " + e.getMessage());
        }
        return processRows(rows);
    }

    private ChildImportResultDto processRows(List<List<String>> rows) {
        if (rows.isEmpty()) {
            throw new BusinessException("File is empty");
        }

        List<String> header = normalizedHeader(rows.get(0));
        int firstNameCol = header.indexOf("firstname");
        int lastNameCol = header.indexOf("lastname");
        int motherNameCol = header.indexOf("mothername");
        int fatherNameCol = header.indexOf("fathername");
        int mobileCol = header.indexOf("mobile");
        int altMobileCol = header.indexOf("alternatemobile");
        int emailCol = header.indexOf("email");
        int ageCol = header.indexOf("age");
        int dobCol = header.indexOf("dateofbirth");
        int addressCol = header.indexOf("address");

        if (firstNameCol == -1 || lastNameCol == -1 || mobileCol == -1) {
            throw new BusinessException(
                    "File must have 'firstName', 'lastName', and 'mobile' columns in the header row (others are optional)");
        }

        Set<String> existingMobiles = childRepository.findAll().stream()
                .map(Child::getMobile)
                .filter(m -> m != null && !m.isBlank())
                .collect(Collectors.toCollection(HashSet::new));

        List<String> errors = new ArrayList<>();
        int created = 0;
        int skipped = 0;
        int processed = 0;

        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            int rowNum = i + 1; // 1-indexed, matches what a spreadsheet shows

            if (row.stream().allMatch(c -> c == null || c.isBlank())) {
                continue; // skip fully blank rows
            }

            String firstName = cell(row, firstNameCol).trim();
            String lastName = cell(row, lastNameCol).trim();
            String mobile = cell(row, mobileCol).trim();

            if (firstName.isBlank() || lastName.isBlank() || mobile.isBlank()) {
                errors.add("Row " + rowNum + ": firstName, lastName and mobile are required");
                continue;
            }
            if (!mobile.matches("^[6-9]\\d{9}$")) {
                errors.add("Row " + rowNum + ": invalid mobile number '" + mobile + "'");
                continue;
            }
            if (existingMobiles.contains(mobile)) {
                errors.add("Row " + rowNum + ": skipped - a child with mobile " + mobile + " already exists");
                skipped++;
                processed++;
                continue;
            }

            try {
                String ageStr = cell(row, ageCol).trim();
                ChildUpsertRequest request = new ChildUpsertRequest(
                        firstName,
                        lastName,
                        emptyToNull(cell(row, motherNameCol)),
                        emptyToNull(cell(row, fatherNameCol)),
                        mobile,
                        emptyToNull(cell(row, altMobileCol)),
                        emptyToNull(cell(row, emailCol)),
                        ageStr.isBlank() ? null : Integer.parseInt(ageStr),
                        emptyToNull(cell(row, dobCol)),
                        emptyToNull(cell(row, addressCol))
                );
                childService.create(request);
                existingMobiles.add(mobile);
                created++;
                processed++;
            } catch (NumberFormatException e) {
                errors.add("Row " + rowNum + ": invalid age value");
            } catch (Exception e) {
                errors.add("Row " + rowNum + ": " + e.getMessage());
            }
        }

        return new ChildImportResultDto(processed, created, skipped, errors);
    }

    private String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
