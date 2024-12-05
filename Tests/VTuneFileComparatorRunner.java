package Tests;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class VTuneFileComparatorRunner {

    public static void CheckCD(String runid) {
        // if (args.length < 2) {
        //     System.out.println("Usage: java VTuneProcessor <NormalRunFilePath> <MarkerRunFilePath>");
        //     return;
        // }
        String runID = runid;
        //String runID = "2024_12_03_18_37_59";

        // Placeholder list of methods to process
        List<String> methods = Arrays.asList(
            "cd__CollisionDetector__handleNewFrame",
            "cd__CollisionDetector__putIntoMap",
            "cd__CollisionDetector__recurse",
            "cd__CollisionDetector__reduceCollisionSet",
            "cd__CollisionDetector$RemoveAbsentAircraft__apply",
            "cd__RedBlackTree__findNode",
            "cd__RedBlackTree__forEach",
            "cd__RedBlackTree__put",
            "cd__RedBlackTree__treeInsert"
        );

        // Loop through each method in the list
        for (String method : methods) {
            String normalRunFilePath = "/home/hb478/repos/GTSlowdownSchedular/Data/" + runID + "_NormalRun/" + method + ".txt";
            String markerRunFilePath = "/home/hb478/repos/GTSlowdownSchedular/Data/" + runID + "_MarkerRun/" + method + ".txt";

            // Extract the method/file name from the provided paths
            String methodName = extractFileName(normalRunFilePath);
            String markerRunFolder = Paths.get(markerRunFilePath).getParent().toString();

            // Construct file paths
            String filteredMarkerRunPath = markerRunFolder + "/" + methodName + "_Filtered_MarkerRun.txt";
            String differencesOutputPath = markerRunFolder + "/" + methodName + "_Differences_Output.txt";

            try {
                // Step 1: Filter the Marker Run file
                System.out.println("Step 1: Filtering Marker Run file for method: " + method);
                VTuneMarkerFilter.filterAndDetectBlocks(markerRunFilePath, filteredMarkerRunPath);

                // Step 2: Compare the Normal Run and Filtered Marker Run files
                System.out.println("Step 2: Comparing files for method: " + method);
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(differencesOutputPath))) {
                    List<String> file1Lines = VTuneLineComparator.normalizeLines(normalRunFilePath);
                    List<String> file2Lines = VTuneLineComparator.normalizeLines(filteredMarkerRunPath);
                    VTuneLineComparator.findDifferences(file1Lines, file2Lines, writer);
                }

                System.out.println("Differences file created for method " + method + " at: " + differencesOutputPath);
            } catch (IOException e) {
                System.err.println("Error processing files for method " + method + ": " + e.getMessage());
            }
        }
    }

    /**
     * Extracts the file name (without extension) from a full file path.
     */
    private static String extractFileName(String filePath) {
        return Paths.get(filePath).getFileName().toString().replaceFirst("[.][^.]+$", "");
    }
}
