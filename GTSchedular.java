
import org.json.JSONObject;

import Phases.Common.CompilerReplayRunner;
import Phases.Divining.DiviningRunner;
import Phases.Divining.DiviningRunnerMultiplexed;
import Phases.Marker.MarkerRunner;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GTSchedular {

    public Boolean lowFootPrint;
    public Boolean compilerReplay;
    public String benchmark;
    public int iterations;
    public String ID;
    public double slowdownAmount;

    // Parameterized constructor
    public GTSchedular(String benchmarkString, int iterations, Boolean lowFootPrint, Boolean compilerReplay, double slowdownAmount) {
        this.benchmark = benchmarkString;
        this.iterations = iterations;
        this.lowFootPrint = lowFootPrint;
        this.compilerReplay = compilerReplay;
        this.slowdownAmount = slowdownAmount;
        ID = generateId();

        schedule();

    }

    /// this method should invoke both the marker and divining phase
    public void schedule() {

       if (compilerReplay) {
           CompilerReplayRunner.run(benchmark, iterations, ID);
        }

        MarkerRunner.run(benchmark, iterations, ID, compilerReplay);
        // ID = "2025_01_07_22_40_12";
        //DiviningRunner.run(benchmark, iterations, ID, lowFootPrint, compilerReplay, slowdownAmount);
        DiviningRunnerMultiplexed.run(benchmark, iterations, ID, lowFootPrint, compilerReplay, slowdownAmount);
        // Divining

        mergeFinalJsonFiles(benchmark, ID);

    }

    public static String generateId() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
        return now.format(formatter);
    }

    public static void mergeFinalJsonFiles(String benchmark, String id) {
        // Directory path based on the ID
        String directoryPath = "Data/" + id + "_SlowDown_Data";
        File directory = new File(directoryPath);

        // JSONObject to hold the merged JSON objects in the desired format
        JSONObject mergedData = new JSONObject();

        if (directory.exists() && directory.isDirectory()) {
            // Filter files starting with "Final_" and with ".json" extension
            File[] files = directory.listFiles((dir, name) -> name.startsWith("Final_") && name.endsWith(".json"));

            if (files != null) {
                // Process each file
                for (File file : files) {
                    try (FileReader reader = new FileReader(file)) {
                        // Read file content into a String
                        StringBuilder content = new StringBuilder();
                        int ch;
                        while ((ch = reader.read()) != -1) {
                            content.append((char) ch);
                        }

                        // Parse content as JSONObject
                        JSONObject fileData = new JSONObject(content.toString());

                        // Merge each method in fileData into mergedData
                        for (String key : fileData.keySet()) {
                            JSONObject methodData = fileData.getJSONObject(key);

                            // If method already exists in mergedData, merge its blocks
                            if (mergedData.has(key)) {
                                JSONObject existingMethodData = mergedData.getJSONObject(key);
                                mergeBlockData(existingMethodData, methodData);
                            } else {
                                // Otherwise, add the new method to mergedData
                                mergedData.put(key, methodData);
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("Error reading JSON file: " + file.getName());
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.out.println("Directory does not exist: " + directoryPath);
            return;
        }

        // Output path for the merged JSON file
        String outputPath = directoryPath + "/Final_"+benchmark+".json";

        try (FileWriter fileWriter = new FileWriter(outputPath)) {
            // Write the merged JSONObject to the output file
            fileWriter.write(mergedData.toString(4)); // Pretty print with an indentation of 4
            System.out.println("Merged JSON data saved to: " + outputPath);
        } catch (IOException e) {
            System.err.println("Error writing merged JSON file.");
            e.printStackTrace();
        }
    }

    // Method to merge block data from newMethodData into existingMethodData
    private static void mergeBlockData(JSONObject existingMethodData, JSONObject newMethodData) {
        for (String blockKey : newMethodData.keySet()) {
            Object newBlockValue = newMethodData.get(blockKey);

            // If block already exists and is a JSONObject (like "Backend Blocks"), merge
            // recursively
            if (existingMethodData.has(blockKey) && newBlockValue instanceof JSONObject) {
                JSONObject existingBlock = existingMethodData.getJSONObject(blockKey);
                mergeBlockData(existingBlock, (JSONObject) newBlockValue);
            } else {
                // Otherwise, add or replace the block
                existingMethodData.put(blockKey, newBlockValue);
            }
        }
    }

    public static void main(String[] args) {
        mergeFinalJsonFiles("Queens", "2024_11_07_14_53_24");
    }

}
