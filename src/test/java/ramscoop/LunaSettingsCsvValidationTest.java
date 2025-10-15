package ramscoop;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Test to validate LunaSettings.csv for common issues that cause LunaLib failures.
 * This prevents runtime crashes from malformed CSV data.
 * Run with: java ramscoop.LunaSettingsCsvValidationTest
 */
public class LunaSettingsCsvValidationTest {

    private static final String CSV_PATH = "data/config/LunaSettings.csv";

    public static void main(String[] args) {
        System.out.println("Running LunaSettingsCsvValidationTest...");

        try {
            testNoUnescapedPercentSignsInTooltips();
            testRequiredFieldsPresent();
            testNumericFieldRanges();
            System.out.println("✅ All LunaSettingsCsvValidationTest tests passed!");
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Test that no tooltip or description fields contain unescaped % characters.
     * LunaLib uses String.format() which treats % as format specifiers, causing
     * MissingFormatArgumentException if not escaped as %%.
     */
    public static void testNoUnescapedPercentSignsInTooltips() throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_PATH))) {
            String line;
            int lineNumber = 0;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (isFirstLine) {
                    isFirstLine = false; // Skip header
                    continue;
                }

                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue; // Skip empty lines and comments
                }

                String[] parts = parseCsvLine(line);
                if (parts.length >= 6) { // tooltip is column 6 (0-indexed)
                    String tooltip = parts[5].trim();
                    if (tooltip.contains("%") && !tooltip.contains("%%")) {
                        throw new Exception("Line " + lineNumber + ": Unescaped % character in tooltip field. " +
                                  "Use %% to escape percent signs for LunaLib compatibility. " +
                                  "Found: '" + tooltip + "'");
                    }
                }

                if (parts.length >= 7) { // description is column 7 (0-indexed)
                    String description = parts[6].trim();
                    if (description.contains("%") && !description.contains("%%")) {
                        throw new Exception("Line " + lineNumber + ": Unescaped % character in description field. " +
                                  "Use %% to escape percent signs for LunaLib compatibility. " +
                                  "Found: '" + description + "'");
                    }
                }
            }
        }
    }

    /**
     * Test that all required fields are present and non-empty for actual settings.
     */
    public static void testRequiredFieldsPresent() throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_PATH))) {
            String line;
            int lineNumber = 0;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (isFirstLine) {
                    isFirstLine = false; // Skip header
                    continue;
                }

                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue; // Skip empty lines and comments
                }

                String[] parts = parseCsvLine(line);
                if (parts.length < 2) {
                    continue; // Skip malformed lines
                }

                String fieldId = parts[0].trim();
                if (fieldId.isEmpty()) {
                    continue; // Skip lines with empty fieldID (empty lines, separators, etc.)
                }

                String fieldName = parts[1].trim();

                // Skip header entries
                if (fieldId.contains("_header") || fieldId.equals("ramscoop_header")) {
                    continue;
                }

                // Check required fields for actual settings
                if (fieldId.isEmpty()) {
                    throw new Exception("Line " + lineNumber + ": fieldID cannot be empty");
                }
                if (fieldName.isEmpty()) {
                    throw new Exception("Line " + lineNumber + ": fieldName cannot be empty");
                }

                if (parts.length >= 3) {
                    String fieldType = parts[2].trim();
                    if (fieldType.isEmpty()) {
                        throw new Exception("Line " + lineNumber + ": fieldType cannot be empty for fieldID '" + fieldId + "'");
                    }
                }
            }
        }
    }

    /**
     * Test that numeric fields have valid min/max values where specified.
     */
    public static void testNumericFieldRanges() throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_PATH))) {
            String line;
            int lineNumber = 0;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (isFirstLine) {
                    isFirstLine = false; // Skip header
                    continue;
                }

                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue; // Skip empty lines and comments
                }

                String[] parts = parseCsvLine(line);
                if (parts.length < 8) {
                    continue; // Not enough columns for min/max
                }

                String fieldId = parts[0].trim();
                String fieldType = parts[2].trim();
                String minValue = parts[6].trim();
                String maxValue = parts[7].trim();

                // Skip header entries and non-numeric types
                if (fieldId.contains("_header") || fieldId.equals("ramscoop_header") ||
                    !fieldType.equals("Double")) {
                    continue;
                }

                // Check that min < max for numeric fields
                if (!minValue.isEmpty() && !maxValue.isEmpty()) {
                    try {
                        double min = Double.parseDouble(minValue);
                        double max = Double.parseDouble(maxValue);
                        if (min >= max) {
                            throw new Exception("Line " + lineNumber + ": minValue (" + min +
                                        ") must be less than maxValue (" + max + ") for fieldID '" + fieldId + "'");
                        }
                    } catch (NumberFormatException e) {
                        throw new Exception("Line " + lineNumber + ": Invalid numeric min/max values for fieldID '" + fieldId +
                                  "': min='" + minValue + "', max='" + maxValue + "'");
                    }
                }
            }
        }
    }

    /**
     * Parse CSV line handling quoted fields properly
     */
    private static String[] parseCsvLine(String line) {
        java.util.List<String> fields = new java.util.ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentField = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString().trim());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        fields.add(currentField.toString().trim());
        return fields.toArray(new String[0]);
    }
}