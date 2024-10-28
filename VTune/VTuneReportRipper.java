package VTune;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VTuneReportRipper {

    public static void main(String[] args) {
        String filePath = "vtune_report.txt";
        VTuneReportRipper ripper = new VTuneReportRipper();
        Map<String, BlockData> blocks = ripper.processFileIntoBlocks(filePath);

        // Output blocks and their Graal ID for verification
        for (Map.Entry<String, BlockData> entry : blocks.entrySet()) {
            System.out.println("Block " + entry.getKey() + ":");
            System.out.println("CPU Time: " + entry.getValue().getCpuTime());
            if (entry.getValue().getGraalID() != null) {
                System.out.println("Graal ID: " + entry.getValue().getGraalID());
            }
            for (String line : entry.getValue().getLines()) {
                System.out.println(line);
            }
            System.out.println();
        }
    }

    // Method to read and process the text file into blocks
    public Map<String, BlockData> processFileIntoBlocks(String filePath) {
        Map<String, BlockData> blocks = new HashMap<>();
        String currentBlock = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Match lines with "Block <number>"
                if (line.matches(".*Block\\s+\\d+.*")) {
                    // New Block found, extract the block identifier
                    currentBlock = line.replaceAll(".*(Block\\s+\\d+).*", "$1").trim();
                    blocks.put(currentBlock, new BlockData());

                    // Check if there's a CPU Time on the same line and extract it
                    String cpuTime = extractCpuTime(line);
                    if (cpuTime != null) {
                        blocks.get(currentBlock).setCpuTime(cpuTime);
                    }
                }

                // Add lines to the current block
                if (currentBlock != null) {
                    blocks.get(currentBlock).addLine(line);
                }
            }

            // After file is read, check each block for the instruction sequence
            for (BlockData block : blocks.values()) {
                String graalID = detectGraalID(block.getLines());
                if (graalID != null) {
                    block.setGraalID(graalID);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        return blocks;
    }

    // Helper method to extract CPU Time from a line (if present)
    private String extractCpuTime(String line) {
        // Regex to match CPU time in the format "0.329s" at the end of the line
        String cpuTimeRegex = ".*\\s(\\d+\\.\\d+s)$";
        if (line.matches(cpuTimeRegex)) {
            return line.replaceAll(cpuTimeRegex, "$1");
        }
        return null; // No CPU time found
    }

    // Helper method to detect the Graal ID by looking for the instruction sequence
    private String detectGraalID(List<String> lines) {
        String pattern = "vshufps xmm0, xmm0, xmm0, ";
        boolean sfenceStart = false;
        List<String> sequence = new ArrayList<>();
        String graalID = null;
    
        for (String line : lines) {
            // Detect sfence start
            if (line.contains("sfence")) {
                if (!sfenceStart) {
                    sfenceStart = true;
                    sequence.clear(); // Start fresh when sfence is encountered
                } else {
                    // sfence end, if sequence contains vshufps instructions, mark it as Backend Block
                    if (!sequence.isEmpty()) {
                        String lastInstruction = sequence.get(sequence.size() - 1).trim();
                        String value = lastInstruction.replaceAll(".*vshufps xmm0, xmm0, xmm0,\\s*([^\\s]+).*", "$1").trim();
                        graalID = "Backend Block " + convertToDecimal(value);
                    }
                    sfenceStart = false; // Reset after end sfence
                }
            }
    
            // Detect vshufps instructions within sfence boundaries or without sfence
            if (line.contains(pattern)) {
                sequence.add(line); // Add matching lines to sequence
                if (!sfenceStart && sequence.size() >= 1) {
                    // Handle case without sfence boundaries
                    String lastInstruction = sequence.get(sequence.size() - 1).trim();
                    String value = lastInstruction.replaceAll(".*vshufps xmm0, xmm0, xmm0,\\s*([^\\s]+).*", "$1").trim();
                    graalID = convertToDecimal(value);
                }
            }
        }
    
        return graalID; // Return Graal ID if found
    }
    

    // Helper method to convert a string (hex or decimal) to a decimal string
    private String convertToDecimal(String value) {
        if (value.startsWith("0x")) {
            // It's a hex value, convert to decimal
            try {
                return String.valueOf(Integer.parseInt(value.substring(2), 16)); // Convert hex to decimal
            } catch (NumberFormatException e) {
                System.err.println("Error parsing hex value: " + value);
            }
        } else {
            // It's already a decimal value
            try {
                return String.valueOf(Integer.parseInt(value)); // Convert decimal string to integer
            } catch (NumberFormatException e) {
                System.err.println("Error parsing decimal value: " + value);
            }
        }
        return null; // In case of any error
    }

    // Inner class to store Block data (lines, CPU Time, and Graal ID)
    public class BlockData {
        private List<String> lines = new ArrayList<>();
        private String cpuTime;
        private String graalID;

        public void addLine(String line) {
            lines.add(line);
        }

        public List<String> getLines() {
            return lines;
        }

        public String getCpuTime() {
            return cpuTime;
        }

        public void setCpuTime(String cpuTime) {
            this.cpuTime = cpuTime;
        }

        public String getGraalID() {
            return graalID;
        }

        public void setGraalID(String graalID) {
            this.graalID = graalID;
        }
    }
}
