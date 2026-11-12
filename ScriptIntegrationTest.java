import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// Console test: loads the tagged script, formats every line with all three
// pronoun sets, and checks that no placeholder is left unresolved.
// Usage: java ScriptIntegrationTest [path-to-script]
public class ScriptIntegrationTest {

    public static void main(String[] args) throws IOException {

        Path path = Path.of(args.length > 0 ? args[0] : "sabay_tayo_script.txt");
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

        int rows = 0, tokenRows = 0, passed = 0, failed = 0;

        for (Pronouns p : Pronouns.values()) {
            System.out.println("\n=== " + p + " ===");

            for (String line : lines) {

                // Skip blank lines and comments.
                if (line.isBlank() || line.startsWith("#")) continue;

                String[] f = line.split("\\|", -1);
                if (f.length != 6) {
                    System.out.println("FAIL: bad column count (" + f.length + "): " + line);
                    failed++;
                    continue;
                }

                // Check the player text, the seatmate reply, and the narration/dialogue text.
                for (int col : new int[]{3, 4}) {
                    String raw = f[col];
                    if (raw.isEmpty()) continue;

                    String out = ScriptFormatter.format(raw, p, "Player", "Seatmate");

                    if (ScriptFormatter.hasUnresolvedTokens(out)) {
                        System.out.println("FAIL [" + f[0] + "] unresolved token: " + out);
                        failed++;
                    } else {
                        passed++;
                    }

                    // Print only lines that actually contained tokens (readable output).
                    if (!raw.equals(out) && p == Pronouns.values()[0]) tokenRows++;
                    if (!raw.equals(out)) {
                        System.out.println("[" + f[0] + "|" + f[1] + "] " + out);
                    }
                }
                if (p == Pronouns.values()[0]) rows++;
            }
        }

        // Extra checks on ScriptFormatter.
        String capTest = ScriptFormatter.format("{They} smiled at {them}.",
                Pronouns.SHE_HER, "Player", "Seatmate");
        boolean capOk = capTest.equals("She smiled at her.");
        System.out.println(capOk ? "PASS: capitalized token" : "FAIL: capitalized token -> " + capTest);
        if (capOk) passed++; else failed++;

        boolean flagOk = ScriptFormatter.hasUnresolvedTokens("Hello {thye}")
                && !ScriptFormatter.hasUnresolvedTokens("Hello there");
        System.out.println(flagOk ? "PASS: unresolved-token detector" : "FAIL: unresolved-token detector");
        if (flagOk) passed++; else failed++;

        try {
            ScriptFormatter.format("Hi {player}", Pronouns.HE_HIM, null, "Seatmate");
            System.out.println("FAIL: null player name");
            failed++;
        } catch (IllegalArgumentException e) {
            System.out.println("PASS: null player name");
            passed++;
        }

        System.out.println("\n===== SCRIPT INTEGRATION SUMMARY =====");
        System.out.println("Script rows read:      " + rows);
        System.out.println("Rows containing tokens: " + tokenRows);
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
    }
}