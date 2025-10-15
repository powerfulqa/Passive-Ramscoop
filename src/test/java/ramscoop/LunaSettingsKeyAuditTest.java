package ramscoop;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Test to verify that all LunaSettings fieldIDs used in ModPlugin.java
 * match the fieldIDs defined in LunaSettings.csv.
 * This prevents LunaLib configuration mismatches that cause settings to fail.
 * Run with: java ramscoop.LunaSettingsKeyAuditTest
 */
public class LunaSettingsKeyAuditTest {

    private static final String CSV_PATH = "data/config/LunaSettings.csv";
    private static final String MOD_PLUGIN_PATH = "src/ramscoop/ModPlugin.java";

    public static void main(String[] args) {
        System.out.println("Running LunaSettingsKeyAuditTest...");

        try {
            testAllLunaSettingsKeysExistInCsv();
            testAllCsvFieldIdsAreUsedInCode();
            System.out.println("✅ All LunaSettingsKeyAuditTest tests passed!");
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Test that all LunaSettings.get* calls in ModPlugin.java use fieldIDs
     * that exist in LunaSettings.csv
     */
    public static void testAllLunaSettingsKeysExistInCsv() throws IOException {
        Set<String> csvFieldIds = parseCsvFieldIds();
        Set<String> codeFieldIds = parseCodeFieldIds();

        // Check that all fieldIDs used in code exist in CSV
        for (String codeFieldId : codeFieldIds) {
            if (!csvFieldIds.contains(codeFieldId)) {
                throw new RuntimeException("LunaSettings fieldID '" + codeFieldId + "' used in ModPlugin.java but not found in LunaSettings.csv");
            }
        }
    }

    /**
     * Test that all fieldIDs in LunaSettings.csv are actually used in ModPlugin.java
     * (helps catch obsolete CSV entries)
     */
    public static void testAllCsvFieldIdsAreUsedInCode() throws IOException {
        Set<String> csvFieldIds = parseCsvFieldIds();
        Set<String> codeFieldIds = parseCodeFieldIds();

        // Filter out header entries that aren't actual settings
        Set<String> actualCsvFieldIds = new HashSet<>();
        for (String fieldId : csvFieldIds) {
            if (!fieldId.contains("_header") && !fieldId.equals("ramscoop_header") && !isKnownUnusedFieldId(fieldId)) {
                actualCsvFieldIds.add(fieldId);
            }
        }

        // Check that all actual CSV fieldIDs are used in code
        for (String csvFieldId : actualCsvFieldIds) {
            if (!codeFieldIds.contains(csvFieldId)) {
                throw new RuntimeException("LunaSettings fieldID '" + csvFieldId + "' defined in LunaSettings.csv but not used in ModPlugin.java");
            }
        }
    }

    /**
     * Parse fieldIDs from LunaSettings.csv
     */
    private static Set<String> parseCsvFieldIds() throws IOException {
        Set<String> fieldIds = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_PATH))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; // Skip header
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length > 0 && !parts[0].trim().isEmpty() && !parts[0].startsWith("#")) {
                    fieldIds.add(parts[0].trim());
                }
            }
        }
        return fieldIds;
    }

    /**
     * Parse fieldIDs from LunaSettings.get* calls in ModPlugin.java
     * Filters out fallback fieldIDs that are no longer in the CSV
     */
    private static Set<String> parseCodeFieldIds() throws IOException {
        Set<String> fieldIds = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(MOD_PLUGIN_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Look for LunaSettings.get*(MOD_ID, "fieldId") patterns
                if (line.contains("LunaSettings.get") && line.contains("MOD_ID")) {
                    int startQuote = line.indexOf("MOD_ID, \"");
                    if (startQuote != -1) {
                        startQuote += 9; // Length of 'MOD_ID, "'
                        int endQuote = line.indexOf("\"", startQuote);
                        if (endQuote != -1) {
                            String fieldId = line.substring(startQuote, endQuote);
                            // Skip known fallback fieldIDs that are no longer in CSV
                            if (!isKnownFallbackFieldId(fieldId)) {
                                fieldIds.add(fieldId);
                            }
                        }
                    }
                }
            }
        }
        return fieldIds;
    }

    /**
     * Check if a fieldID is a known fallback that's no longer in the CSV
     */
    private static boolean isKnownFallbackFieldId(String fieldId) {
        return fieldId.equals("ramscoop_no_crew_rate_flat") ||
               fieldId.equals("ramscoop_crew_usage") ||
               fieldId.equals("ramscoop_no_crew_gen") ||
               fieldId.equals("ramscoop_no_crew_rate_percent");
    }

    /**
     * Check if a fieldID is known to be unused (CSV has it but code doesn't implement it)
     */
    private static boolean isKnownUnusedFieldId(String fieldId) {
        return fieldId.equals("nebula_enable_fuel");
    }
}