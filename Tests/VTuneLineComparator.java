package Tests;

import java.io.*;
import java.util.*;

public class VTuneLineComparator {

    public static void main(String[] args) {
        // Define file paths
        String file1Path = "/path/to/NormalRun.txt";
        String file2Path = "/path/to/MarkerRun.txt";
        String outputFilePath = "/path/to/Differences_Output.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            List<String> file1Lines = normalizeLines(file1Path);
            List<String> file2Lines = normalizeLines(file2Path);

            findDifferences(file1Lines, file2Lines, writer);

            System.out.println("Output written to: " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage());
        }
    }

    /**
     * Normalizes lines by removing all instances of memory addresses and timing information.
     */
    public static List<String> normalizeLines(String filePath) throws IOException {
        List<String> normalizedLines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                // Normalize line by removing memory addresses and trailing timing information
                String normalizedLine = line
                        .replaceAll("0x[0-9a-fA-F]+", "")  // Remove memory addresses
                        .replaceAll("\\s+\\d+(\\.\\d+)?s$", "")  // Remove trailing timing info like "0.001s"
                        .trim();
                normalizedLines.add(normalizedLine);
            }
        }
        return normalizedLines;
    }

    /**
     * Compares two lists of lines, prints differences, and checks final block numbers.
     */
    public static void findDifferences(List<String> file1Lines, List<String> file2Lines, BufferedWriter writer) throws IOException {
        int minLength = Math.min(file1Lines.size(), file2Lines.size());

        boolean differencesFound = false;
        int finalBlockFile1 = -1;
        int finalBlockFile2 = -1;

        // Compare line by line
        for (int i = 0; i < minLength; i++) {
            String line1 = file1Lines.get(i);
            String line2 = file2Lines.get(i);

            // Check if the line contains a "Block <number>"
            if (line1.matches(".*(?<!<)Block\\s+\\d+(?!>).*")) {
                finalBlockFile1 = extractBlockNumber(line1);
            }
            if (line2.matches(".*(?<!<)Block\\s+\\d+(?!>).*")) {
                finalBlockFile2 = extractBlockNumber(line2);
            }

            // Compare lines
            if (!line1.equals(line2)) {
                String differenceMessage = "Difference detected at line " + (i + 1) + ":\n" +
                                            "File 1: " + line1 + "\n" +
                                            "File 2: " + line2 + "\n";
                //System.out.print(differenceMessage);
                writer.write(differenceMessage);
                differencesFound = true;
            }
        }

        // Check for extra lines in file1
        if (file1Lines.size() > minLength) {
            String extraLinesMessage = "Extra lines in File 1:\n";
            //System.out.print(extraLinesMessage);
            writer.write(extraLinesMessage);
            for (int i = minLength; i < file1Lines.size(); i++) {
                String lineMessage = "Line " + (i + 1) + ": " + file1Lines.get(i) + "\n";
                //System.out.print(lineMessage);
                writer.write(lineMessage);
                if (file1Lines.get(i).matches(".*(?<!<)Block\\s+\\d+(?!>).*")) {
                    finalBlockFile1 = extractBlockNumber(file1Lines.get(i));
                }
            }
            differencesFound = true;
        }

        // Check for extra lines in file2
        if (file2Lines.size() > minLength) {
            String extraLinesMessage = "Extra lines in File 2:\n";
            //System.out.print(extraLinesMessage);
            writer.write(extraLinesMessage);
            for (int i = minLength; i < file2Lines.size(); i++) {
                String lineMessage = "Line " + (i + 1) + ": " + file2Lines.get(i) + "\n";
                //System.out.print(lineMessage);
                writer.write(lineMessage);
                if (file2Lines.get(i).matches(".*(?<!<)Block\\s+\\d+(?!>).*")) {
                    finalBlockFile2 = extractBlockNumber(file2Lines.get(i));
                }
            }
            differencesFound = true;
        }

        // Check for final block number differences
        if (finalBlockFile1 != finalBlockFile2) {
            String finalBlockDifferenceMessage = "Final block number mismatch:\n" +
                                                 "File 1: " + finalBlockFile1 + "\n" +
                                                 "File 2: " + finalBlockFile2 + "\n";
            System.out.print(finalBlockDifferenceMessage);
            writer.write(finalBlockDifferenceMessage);
        } else {
            String finalBlockMatchMessage = "Final block numbers are the same: " + finalBlockFile1 + "\n";
            System.out.print(finalBlockMatchMessage);
            writer.write(finalBlockMatchMessage);
        }

        if (!differencesFound) {
            String noDifferenceMessage = "No differences found.\n";
            System.out.print(noDifferenceMessage);
            writer.write(noDifferenceMessage);
        }
    }

    /**
     * Extracts the block number from a "Block <number>" line.
     */
    private static int extractBlockNumber(String line) {
        String[] parts = line.split("\\s+");
        return Integer.parseInt(parts[parts.length-1]);
        // for (String part : parts) {
        //     if (part.startsWith("Block")) {
        //         try {
        //             return Integer.parseInt(part.replace("Block", "").trim());
        //         } catch (NumberFormatException e) {
        //             // Ignore and continue
        //         }
        //     }
        // }
        // return -1; // Return -1 if no block number found
    }
}
