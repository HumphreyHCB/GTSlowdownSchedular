package Tests;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class VTuneMarkerFilter {

    public static void main(String[] args) {
        // Specify input and output file paths
        String inputFilePath = "/home/hb478/repos/GTSlowdownSchedular/Data/2024_12_03_12_47_25_MarkerRun/cd__CollisionDetector__handleNewFrame.txt";
        String outputFilePath = "/home/hb478/repos/GTSlowdownSchedular/Data/2024_12_03_12_47_25_MarkerRun/cd__CollisionDetector__handleNewFrame_Filtered_MarkerRun.txt";

        try {
            filterAndDetectBlocks(inputFilePath, outputFilePath);
            System.out.println("Filtered file created at: " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
        }
    }

    /**
     * Reads the input VTune file, filters out unwanted lines, detects consecutive block lines,
     * and writes the result to a new file.
     */
    public static void filterAndDetectBlocks(String inputFilePath, String outputFilePath) throws IOException {
        Pattern markerPattern = Pattern.compile("(vpblendd xmm0, xmm0, xmm0,|sfence)");
        String blockRegex = ".*(?<!<)Block\\s+\\d+(?!>).*"; // Matches lines with "Block <number>" not enclosed by "<" and ">"
        List<String> filteredLines = new ArrayList<>();
    
        String previousLine = null;
    
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            String line;
    
            while ((line = reader.readLine()) != null) {
                // Normalize the line
                line = line.trim();
    
                // Skip lines matching the marker pattern
                if (markerPattern.matcher(line).find()) {
                    continue;
                }
    
                // Check for consecutive "Block <number>" lines
                if (previousLine != null && previousLine.matches(blockRegex) && line.matches(blockRegex)) {
                    System.out.println("Consecutive blocks detected:");
                    System.out.println(previousLine);
                    System.out.println(line);
                }
    
                // Add line to filtered list and update the previous line
                filteredLines.add(line);
                previousLine = line;
            }
    
            // Write filtered lines to the output file
            for (String filteredLine : filteredLines) {
                writer.write(filteredLine);
                writer.newLine();
            }
        }
    }
}
