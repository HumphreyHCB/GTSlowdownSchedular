package Phases.Divining;

import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GTBuildSlowdownFile {

    // Store data with method names as keys and block/value pairs as sub-keys/values
    private static final Map<String, Map<String, Integer>> slowdownData = new HashMap<>();

    /**
     * Adds an entry to the slowdown data.
     *
     * @param methodName the method name in format like "Queens::placeQueen"
     * @param vtuneBlock the vtune block number as an integer
     * @param value      the associated integer value for the block
     */
    public static void addEntry(String methodName, int vtuneBlock, int value) {
        // Convert "Queens::placeQueen" to "Queens.placeQueen"
        String formattedMethodName = methodName.replace("::", ".");
        
        // Format the Vtune block entry as "BlockNumber (Vtune Block BlockNumber)"
        String blockKey = vtuneBlock + " (Vtune Block " + vtuneBlock + ")";

        // Ensure method-specific map exists, then add the entry
        slowdownData
                .computeIfAbsent(formattedMethodName, k -> new HashMap<>())
                .put(blockKey, value);
    }

    /**
     * Writes the slowdown data to a JSON file at the specified location.
     *
     * @param filePath the file path where the JSON file should be saved
     */
    public static void writeToFile(String filePath) {
        JSONObject jsonObject = new JSONObject(slowdownData);
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(jsonObject.toString(2)); // Pretty print with indentation
            System.out.println("JSON data saved successfully to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Example usage of the class
    public static void main(String[] args) {
        // Adding entries
        addEntry("Queens::placeQueen", 3, 11);
        addEntry("Queens::placeQueen", 5, 0);
        addEntry("Queens::placeQueen", 6, 4);
        addEntry("Queens::setRowColumn", 0, 1);
        addEntry("Queens::getRowColumn", 3, 3);
        addEntry("Queens::getRowColumn", 4, 5);

        // Specify the location to save the JSON file
        String filePath = "Data/SlowdownData.json";
        writeToFile(filePath);
    }
}
