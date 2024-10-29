package Phases.Divining;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class GTBuildSlowdownFile {

    // Store data with method names as keys and block/value pairs as sub-keys/values
    public static final Map<String, Map<String, Integer>> slowdownData = new HashMap<>();

    /**
     * Adds an entry to the slowdown data.
     *
     * @param methodName the method name in format like "Queens::placeQueen"
     * @param vtuneBlock the vtune block number as an integer
     * @param value      the associated integer value for the block
     */
    public static void addEntry(String methodName, int graalID, int vtuneBlock, int slowdown) {
        // Convert "Queens::placeQueen" to "Queens.placeQueen"
        String formattedMethodName = methodName.replace("::", ".");
        
        // Format the Vtune block entry as "BlockNumber (Vtune Block BlockNumber)"
        String blockKey = graalID + " (Vtune Block " + vtuneBlock + ")";

        // Ensure method-specific map exists, then add the entry
        slowdownData
                .computeIfAbsent(formattedMethodName, k -> new HashMap<>())
                .put(blockKey, slowdown);
    }

    /**
     * Writes the slowdown data to a JSON file at the specified location.
     *
     * @param filename the file name for the JSON file
     * @param runID    the ID for the run, used to create a directory path
     */
    public static String writeToFile(String filename, String runID) {
        JSONObject jsonObject = new JSONObject(slowdownData);
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
            //System.out.println("JSON data saved successfully to " + directoryPath + "/" + filename + ".json");
            File file2 = new File(directoryPath + "/" + filename + ".json");
            return file2.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return " ";
        }
    }

    // Example usage of the class
    public static void main(String[] args) {
        // Adding entries
        // addEntry("Queens::placeQueen", 3, 11);
        // addEntry("Queens::placeQueen", 3, 12);
        // addEntry("Queens::placeQueen", 3, 13);
        // addEntry("Queens::placeQueen", 3, 14);
        // addEntry("Queens::placeQueen", 3, 15);

        // Specify the location to save the JSON file
        String filename = "ab";
        writeToFile(filename, "2024_10_29_18_19_36");
    }
}
