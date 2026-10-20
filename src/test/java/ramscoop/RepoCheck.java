package ramscoop;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Repo checks with no game dependency, run by CI from the repo root:
 * - LunaSettings.csv is well formed and its keys match ModPlugin's LunaSettings.get* calls
 * - mod_info.json and Ramscoop.version agree on the version and download URL
 * - with a tag argument (release): the tag matches and changelog.txt has a dated entry for it
 * Run with: java ramscoop.RepoCheck [vX.Y.Z]
 */
public class RepoCheck {
    private static final List<String> HEADER = Arrays.asList("fieldID", "fieldName", "fieldType", "defaultValue",
            "secondaryValue", "fieldDescription", "minValue", "maxValue", "tab");
    // LunaLib passes descriptions through String.format, so a literal % must be written %%.
    private static final Pattern LONE_PERCENT = Pattern.compile("(?<!%)%(?!%)");

    private static final List<String> errors = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        checkSettings();
        String version = checkVersions();
        if (args.length > 0) {
            checkRelease(args[0], version);
        }
        if (!errors.isEmpty()) {
            errors.forEach(e -> System.err.println("FAIL: " + e));
            System.exit(1);
        }
        System.out.println("RepoCheck passed (version " + version + ")");
    }

    private static void checkSettings() throws Exception {
        List<String> lines = Files.readAllLines(Paths.get("data/config/LunaSettings.csv"), StandardCharsets.UTF_8);
        if (!parseCsv(lines.get(0)).equals(HEADER)) {
            errors.add("LunaSettings.csv header must be " + String.join(",", HEADER));
        }
        Set<String> csvKeys = new TreeSet<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().isEmpty() || line.startsWith("#")) {
                continue;
            }
            String where = "LunaSettings.csv line " + (i + 1) + ": ";
            List<String> f = parseCsv(line);
            if (f.size() != HEADER.size()) {
                errors.add(where + f.size() + " columns, expected " + HEADER.size());
                continue;
            }
            String id = f.get(0), type = f.get(2), def = f.get(3);
            if (id.isEmpty() || f.get(1).isEmpty() || type.isEmpty() || f.get(8).isEmpty()) {
                errors.add(where + "fieldID, fieldName, fieldType and tab are required");
            }
            if (LONE_PERCENT.matcher(f.get(5)).find()) {
                errors.add(where + "write % as %% in the description");
            }
            if (type.equals("Header")) {
                continue;
            }
            csvKeys.add(id);
            if (type.equals("Double")) {
                double min = Double.parseDouble(f.get(6)), max = Double.parseDouble(f.get(7));
                double d = Double.parseDouble(def);
                if (min >= max || d < min || d > max) {
                    errors.add(where + id + " default " + def + " outside " + min + ".." + max);
                }
            } else if (type.equals("Radio")) {
                List<String> options = Arrays.asList(f.get(4).split("\\s*,\\s*"));
                if (!options.contains(def)) {
                    errors.add(where + id + " default " + def + " not in options " + options);
                }
            }
        }

        String plugin = read("src/ramscoop/ModPlugin.java");
        Set<String> codeKeys = new TreeSet<>();
        Matcher m = Pattern.compile("LunaSettings\\.get\\w+\\(MOD_ID, \"([^\"]+)\"").matcher(plugin);
        while (m.find()) {
            codeKeys.add(m.group(1));
        }
        for (String k : codeKeys) {
            if (!csvKeys.contains(k)) {
                errors.add("ModPlugin reads " + k + " but LunaSettings.csv has no such setting");
            }
        }
        for (String k : csvKeys) {
            if (!codeKeys.contains(k)) {
                errors.add("LunaSettings.csv defines " + k + " but ModPlugin never reads it");
            }
        }
    }

    private static String checkVersions() throws Exception {
        String modInfo = read("mod_info.json");
        String versionFile = read("Ramscoop.version");
        String version = find(modInfo, "\"version\"\\s*:\\s*\"([^\"]+)\"");
        String fileVersion = find(versionFile, "\"major\"\\s*:\\s*(\\d+)") + "."
                + find(versionFile, "\"minor\"\\s*:\\s*(\\d+)") + "." + find(versionFile, "\"patch\"\\s*:\\s*(\\d+)");
        if (!version.equals(fileVersion)) {
            errors.add("mod_info.json is " + version + " but Ramscoop.version is " + fileVersion);
        }
        // Players may remove the mod from a save: keep it a utility mod and keep its classes out of saves.
        if (!Pattern.compile("\"utility\"\\s*:\\s*\"?true").matcher(modInfo).find()) {
            errors.add("mod_info.json must keep \"utility\":true so the mod can be removed from saves");
        }
        String code = read("src/ramscoop/ModPlugin.java") + read("src/ramscoop/Ramscoop.java");
        // addListener(x) is saved; addListener(x, true) is transient and allowed.
        for (String saved : Arrays.asList("\\.addScript\\(", "\\.addListener\\([^,()]*\\)", "getPersistentData",
                "\\.getMemory(WithoutUpdate)?\\(\\)\\.set\\(")) {
            if (Pattern.compile(saved).matcher(code).find()) {
                errors.add("src/ramscoop matches " + saved + ", which writes to the save; use the transient variant");
            }
        }
        String zip = "/v" + version + "/Ramscoop-v" + version + ".zip\"";
        if (!modInfo.contains(zip) || !versionFile.contains(zip)) {
            errors.add("directDownloadURL in mod_info.json and Ramscoop.version must end in " + zip);
        }
        return version;
    }

    private static void checkRelease(String tag, String version) throws Exception {
        if (!tag.equals("v" + version)) {
            errors.add("tag " + tag + " does not match mod version " + version);
        }
        String changelog = read("changelog.txt");
        if (!Pattern.compile("(?m)^Version " + Pattern.quote(version) + " \\(\\d{4}-\\d{2}-\\d{2}\\)").matcher(changelog).find()
                || changelog.contains("(unreleased)")) {
            errors.add("changelog.txt needs 'Version " + version + " (YYYY-MM-DD)' and no '(unreleased)' entry");
        }
    }

    /** Splits one CSV line, honouring double quotes ("" is an escaped quote). Fields are trimmed. */
    private static List<String> parseCsv(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (c == ',' && !quoted) {
                out.add(cur.toString().trim());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        out.add(cur.toString().trim());
        return out;
    }

    private static String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    private static String find(String text, String regex) {
        Matcher m = Pattern.compile(regex).matcher(text);
        if (!m.find()) {
            errors.add("could not find " + regex);
            return "?";
        }
        return m.group(1);
    }
}
