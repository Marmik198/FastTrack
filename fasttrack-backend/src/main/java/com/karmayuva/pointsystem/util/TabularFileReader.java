package com.karmayuva.pointsystem.util;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Reads an uploaded spreadsheet-ish file (.csv, .txt, .xlsx/.xls) into a plain
 * List<List<String>> of rows/cells, so callers don't need to care which format
 * the admin actually uploaded. Row 0 is expected to be the header row.
 */
@Component
public class TabularFileReader {

    public List<List<String>> read(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase(Locale.ROOT) : "";
        if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
            return readExcel(file);
        }
        return readDelimited(file);
    }

    private List<List<String>> readDelimited(MultipartFile file) throws Exception {
        // Auto-detect comma vs tab delimiter from the header line
        String firstLine;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
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

    public static String cell(List<String> row, int index) {
        if (index < 0 || index >= row.size() || row.get(index) == null) return "";
        return row.get(index);
    }

    public static List<String> normalizedHeader(List<String> rawHeader) {
        return rawHeader.stream().map(h -> h == null ? "" : h.trim().toLowerCase(Locale.ROOT)).toList();
    }
}
