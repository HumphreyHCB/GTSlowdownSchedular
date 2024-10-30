package Phases.Divining;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import Phases.Divining.MarkerPhaseDataLookup.BlockInfo;

public class DiviningRunner {
    
    public static void run(String Benchmark, int iterations, String RunID) {
        // Load the data from the Marker Phase into static memory
        MarkerPhaseDataLookup.loadData(RunID);

        // Get a list of all the methods we will be "attacking"
        List<String> methods = MarkerPhaseDataLookup.getAllMethods();
        for (String method : methods) {
            
            List<BlockInfo> blocks = MarkerPhaseDataLookup.getBenchmarkEntries(method.replace(".", "::"));
            Map<String, Map<String, Integer>> MethodsSlowdownData = new HashMap<>();
            Map<String, Map<String, Integer>> MethodsBackendSlowdownData = new HashMap<>();

            // Use a consistent format for the method name
            String formattedMethodName = method.replace(".", "::");
            
            for (BlockInfo blockInfo : blocks) {
                // Run the Divine function for the block
                Diviner.Divine(RunID, formattedMethodName, blockInfo, Benchmark, iterations);
            
                // Generate block key in the same format as addEntry
                String blockKey = blockInfo.graalID + " (Vtune Block " + blockInfo.vtuneBlock + ")";
                
                // Copy regular slowdown data for this block
                int slowdownValue = GTBuildSlowdownFile.slowdownData
                                        .getOrDefault(method.replace("::", "."), new HashMap<>())
                                        .getOrDefault(blockKey, 0);
                
                // Add a copy of regular block data to MethodsSlowdownData
                MethodsSlowdownData
                        .computeIfAbsent(formattedMethodName.replace("::", "."), k -> new HashMap<>())
                        .put(blockKey, slowdownValue);
                
                // Copy backend slowdown data for this block if it exists
                int backendSlowdownValue = GTBuildSlowdownFile.backendSlowdownData
                                               .getOrDefault(method.replace("::", "."), new HashMap<>())
                                               .getOrDefault(blockKey, 0);
                
                if (backendSlowdownValue != 0) { // Only add if there is a backend slowdown value
                    MethodsBackendSlowdownData
                            .computeIfAbsent(formattedMethodName.replace("::", "."), k -> new HashMap<>())
                            .put(blockKey, backendSlowdownValue);
                }

                // Clear the slowdown data after processing each block to avoid reference issues
                GTBuildSlowdownFile.slowdownData.clear();
                GTBuildSlowdownFile.backendSlowdownData.clear();
            }
            
            // Reconstruct the slowdown data from all blocks and write to the final file
            for (Map.Entry<String, Map<String, Integer>> entry : MethodsSlowdownData.entrySet()) {
                GTBuildSlowdownFile.slowdownData.computeIfAbsent(entry.getKey(), k -> new HashMap<>()).putAll(entry.getValue());
            }

            for (Map.Entry<String, Map<String, Integer>> entry : MethodsBackendSlowdownData.entrySet()) {
                GTBuildSlowdownFile.backendSlowdownData.computeIfAbsent(entry.getKey(), k -> new HashMap<>()).putAll(entry.getValue());
            }

            GTBuildSlowdownFile.writeToFile("Final_" + formattedMethodName.replace("::", "."), RunID);
        }
    }
}
