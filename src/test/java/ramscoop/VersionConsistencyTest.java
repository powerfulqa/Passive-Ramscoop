package ramscoop;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Test to verify version consistency across mod_info.json, version.json, and Ramscoop.version.
 * This ensures TriOS compatibility and prevents version mismatches in releases.
 * Run with: java ramscoop.VersionConsistencyTest
 */
public class VersionConsistencyTest {

    public static void main(String[] args) {
        System.out.println("Running VersionConsistencyTest...");

        try {
            testVersionConsistency();
            System.out.println("✅ Version consistency test passed!");
        } catch (Exception e) {
            System.err.println("❌ Version consistency test failed: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Test that all version files report the same semantic version
     */
    public static void testVersionConsistency() throws Exception {
        String modInfoVersion = getVersionFromModInfo();
        String versionJsonVersion = getVersionFromVersionJson();
        String ramscoopVersion = getVersionFromRamscoopVersion();

        if (!modInfoVersion.equals(versionJsonVersion)) {
            throw new RuntimeException("Version mismatch: mod_info.json=" + modInfoVersion +
                                     ", version.json=" + versionJsonVersion);
        }

        if (!versionJsonVersion.equals(ramscoopVersion)) {
            throw new RuntimeException("Version mismatch: version.json=" + versionJsonVersion +
                                     ", Ramscoop.version=" + ramscoopVersion);
        }

        System.out.println("All versions consistent: " + modInfoVersion);
    }

    private static String getVersionFromModInfo() throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader("mod_info.json"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("\"version\":")) {
                    // Extract version value
                    int colonIndex = line.indexOf(":");
                    int quoteStart = line.indexOf("\"", colonIndex);
                    int quoteEnd = line.indexOf("\"", quoteStart + 1);
                    if (quoteStart != -1 && quoteEnd != -1) {
                        return line.substring(quoteStart + 1, quoteEnd);
                    }
                }
            }
        }
        throw new RuntimeException("Could not find version in mod_info.json");
    }

    private static String getVersionFromVersionJson() throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader("version.json"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("\"version\":")) {
                    int colonIndex = line.indexOf(":");
                    int quoteStart = line.indexOf("\"", colonIndex);
                    int quoteEnd = line.indexOf("\"", quoteStart + 1);
                    if (quoteStart != -1 && quoteEnd != -1) {
                        return line.substring(quoteStart + 1, quoteEnd);
                    }
                }
            }
        }
        throw new RuntimeException("Could not find version in version.json");
    }

    private static String getVersionFromRamscoopVersion() throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader("Ramscoop.version"))) {
            String line;
            int major = -1, minor = -1, patch = -1;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#")) continue; // Skip comments

                if (line.contains("\"major\":")) {
                    major = extractIntValue(line);
                } else if (line.contains("\"minor\":")) {
                    minor = extractIntValue(line);
                } else if (line.contains("\"patch\":")) {
                    patch = extractIntValue(line);
                }
            }

            if (major == -1 || minor == -1 || patch == -1) {
                throw new RuntimeException("Could not parse version from Ramscoop.version");
            }

            return major + "." + minor + "." + patch;
        }
    }

    private static int extractIntValue(String line) {
        int colonIndex = line.indexOf(":");
        int commaIndex = line.indexOf(",", colonIndex);
        if (commaIndex == -1) commaIndex = line.length();
        String valueStr = line.substring(colonIndex + 1, commaIndex).trim();
        return Integer.parseInt(valueStr);
    }
}