package com.supplychainx.util;

import com.supplychainx.exception.ValidationException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Utility for exporting reporting data and grid tables to CSV format.
 */
public class CsvExportUtil {

    public static void exportToCsv(File file, List<String> headers, List<List<String>> rows) {
        if (file == null) {
            throw new ValidationException("Target destination file is required.");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            // Write headers
            writer.write(String.join(",", escapeRow(headers)));
            writer.newLine();

            // Write rows
            for (List<String> row : rows) {
                writer.write(String.join(",", escapeRow(row)));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to export data to CSV: " + e.getMessage(), e);
        }
    }

    private static List<String> escapeRow(List<String> values) {
        return values.stream().map(v -> {
            if (v == null) return "\"\"";
            String escaped = v.replace("\"", "\"\"");
            if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
                return "\"" + escaped + "\"";
            }
            return escaped;
        }).toList();
    }
}
