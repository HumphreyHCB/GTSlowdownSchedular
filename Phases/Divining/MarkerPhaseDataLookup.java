package Phases.Divining;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MarkerPhaseDataLookup {

    private static JSONObject benchmarkData;

    // Static method to initialize and load JSON data
    public static void loadData(String ID) {
        try {
            String filePath = "Data/" + ID + "_MarkerRun/MarkerPhaseInfo.json";
            String jsonString = new String(Files.readAllBytes(Paths.get(filePath)));
            benchmarkData = new JSONObject(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to get all method names (keys) in the JSON file
    public static List<String> getAllMethods() {
        List<String> methods = new ArrayList<>();
        if (benchmarkData != null) {
            Iterator<String> keys = benchmarkData.keys();
            while (keys.hasNext()) {
                methods.add(keys.next());
            }
        } else {
            System.out.println("Benchmark data is not loaded. Please call loadData() first.");
        }
        return methods;
    }

    // Data class to hold benchmark entry details
    public static class BlockInfo {
        public double baseCpuTime;
        public int graalID;
        public boolean backendBlock;
        public int vtuneBlock;
        public int lineCount; // New field for LineCount

        public BlockInfo(double baseCpuTime, int graalID, boolean backendBlock, int vtuneBlock, int lineCount) {
            this.baseCpuTime = baseCpuTime;
            this.graalID = graalID;
            this.backendBlock = backendBlock;
            this.vtuneBlock = vtuneBlock;
            this.lineCount = lineCount;
        }
    }

    // Method to get benchmark entries for a given method name
    public static List<BlockInfo> getBenchmarkEntries(String methodName) {
        List<BlockInfo> entries = new ArrayList<>();

        if (benchmarkData != null && benchmarkData.has(methodName)) {
            JSONArray jsonArray = benchmarkData.getJSONArray(methodName);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonEntry = jsonArray.getJSONObject(i);
                double baseCpuTime = jsonEntry.getDouble("BaseCpuTime");
                int graalID = jsonEntry.getInt("GraalID");
                boolean backendBlock = jsonEntry.getBoolean("Backend Block");
                int vtuneBlock = jsonEntry.getInt("VtuneBlock");
                int lineCount = jsonEntry.getInt("LineCount"); // Read the new LineCount field

                entries.add(new BlockInfo(baseCpuTime, graalID, backendBlock, vtuneBlock, lineCount));
            }
        } else {
            System.out.println("Method not found in the JSON data: " + methodName);
        }

        return entries;
    }

    // Example: retrieve entries based on method name
    public static void main(String[] args) {
        loadData("2024_10_29_18_19_36");
        List<BlockInfo> entries = MarkerPhaseDataLookup.getBenchmarkEntries("Queens::getRowColumn");

        // Print out each entry
        for (BlockInfo entry : entries) {
            System.out.println("BaseCpuTime: " + entry.baseCpuTime +
                               ", GraalID: " + entry.graalID +
                               ", Backend Block: " + entry.backendBlock +
                               ", VtuneBlock: " + entry.vtuneBlock +
                               ", LineCount: " + entry.lineCount); // Print the new LineCount field
        }
    }
}
