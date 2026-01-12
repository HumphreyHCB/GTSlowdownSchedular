import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.regex.*;

public class RawBlockInstructionTimes {

    // Defaults (override with args[0]=input, args[1]=output)
    private static final String DEFAULT_IN  = "queensrawRip.txt";
    private static final String DEFAULT_OUT = "BlockInstructionTimes.csv";

    // Patterns
    private static final Pattern P_BLOCK_HDR = Pattern.compile("^Block\\s+Block\\s+(\\d+)\\s*:\\s*$");
    private static final Pattern P_CPU_TIME  = Pattern.compile("^CPU\\s+Time:\\s*(null|\\d+(?:\\.\\d+)?)\\s*$",
                                                               Pattern.CASE_INSENSITIVE);
    // Addressed line with optional trailing time " ...  0.015s"
    // We capture the full text (group 2) and optional numeric seconds (group 3).
    private static final Pattern P_LINE = Pattern.compile("^(0x[0-9a-fA-F]+)\\s+(.*?)(?:\\s+(\\d+(?:\\.\\d+)?)s\\s*)?$");

    // Matches a bare "Block <id>" line text (no extras)
    private static final Pattern P_BARE_BLOCK_LINE = Pattern.compile("^Block\\s+(\\d+)\\s*$");

    public static void main(String[] args) {
        Path in  = Paths.get(args.length > 0 ? args[0] : DEFAULT_IN);
        Path out = Paths.get(args.length > 1 ? args[1] : DEFAULT_OUT);

        try (BufferedReader br = Files.newBufferedReader(in, StandardCharsets.UTF_8);
             BufferedWriter bw = Files.newBufferedWriter(out, StandardCharsets.UTF_8,
                     StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {

            // CSV header (no address)
            writeCsvRow(bw, "block_id", "block_cpu_time", "line_text", "line_time");

            String line;
            String currentBlockId = null;
            String currentBlockCpuTime = null;

            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;

                // New block header
                Matcher mBlock = P_BLOCK_HDR.matcher(trimmed);
                if (mBlock.matches()) {
                    currentBlockId = mBlock.group(1);
                    currentBlockCpuTime = null;
                    continue;
                }

                // CPU time line
                Matcher mCpu = P_CPU_TIME.matcher(trimmed);
                if (mCpu.matches()) {
                    currentBlockCpuTime = "null".equalsIgnoreCase(mCpu.group(1)) ? "" : mCpu.group(1);
                    continue;
                }

                // Instruction / addressed line (includes a possible "Block <id>" line with time)
                Matcher mLine = P_LINE.matcher(trimmed);
                if (mLine.matches()) {
                    if (currentBlockId == null) continue; // ignore lines before a block

                    String text = mLine.group(2).trim();
                    String tsec = mLine.group(3); // may be null

                    // Skip the addressed "Block <id>" row
                    Matcher bare = P_BARE_BLOCK_LINE.matcher(text);
                    if (bare.matches()) {
                        // only skip if it refers to the current block id
                        String idInText = bare.group(1);
                        if (idInText.equals(currentBlockId)) continue;
                    }

                    writeCsvRow(bw,
                            currentBlockId,
                            nz(currentBlockCpuTime),
                            text,
                            tsec == null ? "" : tsec
                    );
                    continue;
                }

                // ignore other noise
            }

            System.out.println("Wrote -> " + out.toAbsolutePath());

        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            System.exit(2);
        }
    }

    private static String nz(String s) { return s == null ? "" : s; }

    // Minimal CSV writer
    private static void writeCsvRow(BufferedWriter bw, String... fields) throws IOException {
        boolean first = true;
        for (String f : fields) {
            if (!first) bw.write(',');
            first = false;
            String val = (f == null ? "" : f).replace("\"", "\"\"");
            bw.write('"'); bw.write(val); bw.write('"');
        }
        bw.newLine();
    }
}
