package Tests;

import java.io.*;
import java.util.*;

public class VTuneLineComparator {

    private static PrintWriter logWriter;

    public static void setLogWriter(PrintWriter w) {
        logWriter = w;
    }

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
                //writer.write(differenceMessage);
                differencesFound = true;
                if (logWriter != null) {
                    //logWriter.println(differenceMessage);
                }
            }
        }

        // Check for extra lines in file1
        if (file1Lines.size() > minLength) {
            String extraLinesMessage = "Extra lines in File 1:\n";
            writer.write(extraLinesMessage);
            if (logWriter != null) {
                logWriter.println(extraLinesMessage);
            }
            for (int i = minLength; i < file1Lines.size(); i++) {
                String lineMessage = "Line " + (i + 1) + ": " + file1Lines.get(i) + "\n";
                //writer.write(lineMessage);
                if (logWriter != null) {
                    //logWriter.println(lineMessage);
                }
                if (file1Lines.get(i).matches(".*(?<!<)Block\\s+\\d+(?!>).*")) {
                    finalBlockFile1 = extractBlockNumber(file1Lines.get(i));
                }
            }
            differencesFound = true;
        }

        // Check for extra lines in file2
        if (file2Lines.size() > minLength) {
            String extraLinesMessage = "Extra lines in File 2:\n";
            writer.write(extraLinesMessage);
            if (logWriter != null) {
                logWriter.println(extraLinesMessage);
            }
            for (int i = minLength; i < file2Lines.size(); i++) {
                String lineMessage = "Line " + (i + 1) + ": " + file2Lines.get(i) + "\n";
                //writer.write(lineMessage);
                if (logWriter != null) {
                    //logWriter.println(lineMessage);
                }
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
            writer.write(finalBlockDifferenceMessage);
            if (logWriter != null) {
                logWriter.println(finalBlockDifferenceMessage);
            }
        } else {
            String finalBlockMatchMessage = "Final block numbers are the same: " + finalBlockFile1 + "\n";
            writer.write(finalBlockMatchMessage);
            if (logWriter != null) {
                logWriter.println(finalBlockMatchMessage);
            }
        }

        if (!differencesFound) {
            String noDifferenceMessage = "No differences found.\n";
            writer.write(noDifferenceMessage);
            if (logWriter != null) {
                logWriter.println(noDifferenceMessage);
            }
        }
    }

    private static int extractBlockNumber(String line) {
        String[] parts = line.split("\\s+");
        return Integer.parseInt(parts[parts.length-1]);
    }
}
