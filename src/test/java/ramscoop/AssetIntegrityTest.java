package ramscoop;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Test to verify asset integrity and file structure.
 * This ensures required files exist and have correct basic structure.
 * Run with: java ramscoop.AssetIntegrityTest
 */
public class AssetIntegrityTest {

    public static void main(String[] args) {
        System.out.println("Running AssetIntegrityTest...");

        try {
            testRequiredFilesExist();
            testModInfoStructure();
            testLunaSettingsCsvExists();
            testJavaSourceFilesExist();
            System.out.println("✅ Asset integrity test passed!");
        } catch (Exception e) {
            System.err.println("❌ Asset integrity test failed: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Test that all required files exist
     */
    public static void testRequiredFilesExist() throws Exception {
        String[] requiredFiles = {
            "mod_info.json",
            "Ramscoop.version",
            "version.json",
            "README.md",
            "LICENSE.txt",
            "data/config/LunaSettings.csv",
            "settings.json",
            "src/ramscoop/ModPlugin.java",
            "src/ramscoop/Ramscoop.java",
            "build.ps1",
            "CHANGELOG.md",
            "changelog.txt"
        };

        for (String filePath : requiredFiles) {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new RuntimeException("Required file missing: " + filePath);
            }
        }

        System.out.println("All required files present");
    }

    /**
     * Test that mod_info.json has required structure
     */
    public static void testModInfoStructure() throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader("mod_info.json"))) {
            String line;
            boolean hasId = false, hasName = false, hasVersion = false, hasDescription = false,
                    hasGameVersion = false, hasAuthor = false;
            String foundId = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("\"id\":")) {
                    hasId = true;
                    int start = line.indexOf("\"", line.indexOf(":") + 1) + 1;
                    int end = line.indexOf("\"", start);
                    foundId = line.substring(start, end);
                } else if (line.startsWith("\"name\":")) {
                    hasName = true;
                } else if (line.startsWith("\"version\":")) {
                    hasVersion = true;
                } else if (line.startsWith("\"description\":")) {
                    hasDescription = true;
                } else if (line.startsWith("\"gameVersion\":")) {
                    hasGameVersion = true;
                } else if (line.startsWith("\"author\":")) {
                    hasAuthor = true;
                }
            }

            if (!hasId || !hasName || !hasVersion || !hasDescription || !hasGameVersion || !hasAuthor) {
                throw new RuntimeException("mod_info.json missing required fields");
            }

            if (!"m561_ramscoop".equals(foundId)) {
                throw new RuntimeException("mod_info.json id should be 'm561_ramscoop', got: " + foundId);
            }
        }

        System.out.println("mod_info.json structure is valid");
    }

    /**
     * Test that LunaSettings.csv exists and is readable
     */
    public static void testLunaSettingsCsvExists() throws Exception {
        File csvFile = new File("data/config/LunaSettings.csv");
        if (!csvFile.exists()) {
            throw new RuntimeException("LunaSettings.csv not found");
        }

        // Basic CSV validation - check header
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String header = reader.readLine();
            if (header == null || !header.contains("fieldID")) {
                throw new RuntimeException("LunaSettings.csv missing proper header");
            }
        }

        System.out.println("LunaSettings.csv exists and has valid header");
    }

    /**
     * Test that Java source files exist and have basic structure
     */
    public static void testJavaSourceFilesExist() throws Exception {
        String[] javaFiles = {
            "src/ramscoop/ModPlugin.java",
            "src/ramscoop/Ramscoop.java"
        };

        for (String filePath : javaFiles) {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new RuntimeException("Java source file missing: " + filePath);
            }

            // Check basic Java structure
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String content = "";
                String line;
                while ((line = reader.readLine()) != null) {
                    content += line + "\n";
                }

                if (!content.contains("package ramscoop;")) {
                    throw new RuntimeException(filePath + " missing package declaration");
                }
                if (!content.contains("public class")) {
                    throw new RuntimeException(filePath + " missing public class declaration");
                }
            }
        }

        System.out.println("Java source files exist and have valid structure");
    }
}