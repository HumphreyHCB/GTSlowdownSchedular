package Tests;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class VTuneFileComparatorRunner {

    private static PrintWriter logWriter;

    public static void CheckCD(String runID, String logFilePath) {
        // Initialize the logWriter for this run
        try {
            if (logWriter != null) {
                logWriter.close();
            }
            logWriter = new PrintWriter(new FileWriter(logFilePath), true);
        } catch (IOException e) {
            throw new RuntimeException("Unable to open log file: " + logFilePath, e);
        }

        // Set the same logWriter for the VTuneMarkerFilter so it shares the same log output
        VTuneMarkerFilter.setLogWriter(logWriter);
        // Also set it for VTuneLineComparator
        VTuneLineComparator.setLogWriter(logWriter);

        String normalRunDirPath = "/home/hb478/repos/GTSlowdownSchedular/Data/" + runID + "_NormalRun";
        String markerRunDirPath = "/home/hb478/repos/GTSlowdownSchedular/Data/" + runID + "_MarkerRun";

        // Get the list of method names dynamically from the NormalRun directory
        List<String> methods = findMethodsInDirectory(normalRunDirPath);

        if (methods.isEmpty()) {
            logWriter.println("No .txt files found in the NormalRun directory for runID: " + runID);
            return;
        }

        // Loop through each method in the list
        for (String method : methods) {
            String normalRunFilePath = normalRunDirPath + "/" + method;
            String markerRunFilePath = markerRunDirPath + "/" + method;

            // Extract the method/file name from the provided paths
            String methodName = extractFileName(normalRunFilePath);
            String markerRunFolder = Paths.get(markerRunFilePath).getParent().toString();

            // Construct file paths
            String filteredMarkerRunPath = markerRunFolder + "/" + methodName + "_Filtered_MarkerRun.txt";
            String differencesOutputPath = markerRunFolder + "/" + methodName + "_Differences_Output.txt";

            try {
                // Step 1: Filter the Marker Run file
                logWriter.println("Step 1: Filtering Marker Run file for method: " + method);
                VTuneMarkerFilter.filterAndDetectBlocks(markerRunFilePath, filteredMarkerRunPath);

                // Step 2: Compare the Normal Run and Filtered Marker Run files
                logWriter.println("Step 2: Comparing files for method: " + method);
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(differencesOutputPath))) {
                    List<String> file1Lines = VTuneLineComparator.normalizeLines(normalRunFilePath);
                    List<String> file2Lines = VTuneLineComparator.normalizeLines(filteredMarkerRunPath);
                    VTuneLineComparator.findDifferences(file1Lines, file2Lines, writer);
                }

                logWriter.println("Differences file created for method " + method + " at: " + differencesOutputPath);
            } catch (IOException e) {
                logWriter.println("Error processing files for method " + method + ": " + e.getMessage());
            }
        }
    }

    private static List<String> findMethodsInDirectory(String directoryPath) {
        List<String> methods = new ArrayList<>();
        try {
            Files.list(Paths.get(directoryPath))
                .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".txt"))
                .forEach(path -> methods.add(path.getFileName().toString()));
        } catch (IOException e) {
            if (logWriter != null) {
                logWriter.println("Error accessing directory: " + directoryPath + " - " + e.getMessage());
            } else {
                System.err.println("Error accessing directory: " + directoryPath + " - " + e.getMessage());
            }
        }
        return methods;
    }

    private static String extractFileName(String filePath) {
        return Paths.get(filePath).getFileName().toString().replaceFirst("[.][^.]+$", "");
    }
}
