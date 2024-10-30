package Phases.Divining;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GTBuildSlowdownFile {

    // Store data with method names as keys and block/value pairs as sub-keys/values
    public static Map<String, Map<String, Integer>> slowdownData = new HashMap<>();
    public static Map<String, Map<String, Integer>> backendSlowdownData = new HashMap<>();

    /**
     * Adds an entry to the slowdown data.
     *
     * @param methodName    the method name in format like "Queens::placeQueen"
     * @param graalID       the graal ID for the block
     * @param vtuneBlock    the vtune block number as an integer
     * @param slowdown      the associated integer value for the block
     * @param isBackendBlock indicates if this block is a backend block
     */
    public static void addEntry(String methodName, int graalID, int vtuneBlock, int slowdown, boolean isBackendBlock) {
        // Convert "Queens::placeQueen" to "Queens.placeQueen"
        String formattedMethodName = methodName.replace("::", ".");

        // Format the Vtune block entry as "BlockNumber (Vtune Block BlockNumber)"
        String blockKey = graalID + " (Vtune Block " + vtuneBlock + ")";

        // Add the entry to the main slowdownData map
        slowdownData
            .computeIfAbsent(formattedMethodName, k -> new HashMap<>())
            .put(blockKey, slowdown);

        // If it's a backend block, also add it to the backendSlowdownData map
        if (isBackendBlock) {
            backendSlowdownData
                .computeIfAbsent(formattedMethodName, k -> new HashMap<>())
                .put(blockKey, slowdown);
        }
    }

    /**
     * Writes the slowdown data and backend slowdown data to a JSON file at the specified location.
     *
     * @param filename the file name for the JSON file
     * @param runID    the ID for the run, used to create a directory path
     */
    public static String writeToFile(String filename, String runID) {
        JSONObject jsonObject = new JSONObject();

        // Merge regular and backend slowdown data into jsonObject
        for (String methodName : slowdownData.keySet()) {
            JSONObject methodData = new JSONObject();

            // Add all regular block entries for this method
            for (Map.Entry<String, Integer> entry : slowdownData.get(methodName).entrySet()) {
                methodData.put(entry.getKey(), entry.getValue());
            }

            // Only add "Backend Blocks" if there are entries for this method
            if (backendSlowdownData.containsKey(methodName)) {
                JSONObject backendBlocks = new JSONObject(backendSlowdownData.get(methodName));
                methodData.put("Backend Blocks", backendBlocks);
            }

            jsonObject.put(methodName, methodData);
        }

        String directoryPath = "Data/" + runID + "_SlowDown_Data";
        File directory = new File(directoryPath);

        // Create the directory if it does not exist
        if (!directory.exists()) {
            boolean dirCreated = directory.mkdirs();
            if (dirCreated) {
                System.out.println("Directory created at " + directoryPath);
            } else {
                System.out.println("Failed to create directory at " + directoryPath);
                return "";
            }
        }

        try (FileWriter file = new FileWriter(directoryPath + "/" + filename + ".json")) {
            file.write(jsonObject.toString(2)); // Pretty print with indentation
            File file2 = new File(directoryPath + "/" + filename + ".json");
            return file2.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return " ";
        }
    }

    // Example usage of the class
    public static void main(String[] args) {
        // Adding entries for testing
        addEntry("Queens::placeQueen", 0, 3, 11, true); // Backend block
        addEntry("Queens::placeQueen", 1, 5, 0, false);
        addEntry("Queens::placeQueen", 8, 6, 4, true); // Backend block
        addEntry("Queens::placeQueen", 16, 8, 5, true); // Backend block
        addEntry("Queens::placeQueen", 24, 10, 6, true); // Backend block
        addEntry("Queens::placeQueen", 32, 12, 7, true); // Backend block
        addEntry("Queens::placeQueen", 48, 16, 10, true); // Backend block
        addEntry("Queens::setRowColumn", 1, 0, 1, true); // Backend block
        addEntry("Queens::getRowColumn", 0, 3, 3, true); // Backend block
        addEntry("Queens::getRowColumn", 1, 4, 5, true); // Backend block
        addEntry("Queens::getRowColumn", 8, 6, 2, true); // Backend block

        // Specify the location to save the JSON file
        String filename = "example";
        writeToFile(filename, "2024_10_30_15_37_49");
    }
}
