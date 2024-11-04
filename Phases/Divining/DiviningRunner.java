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
            
            // Use a consistent format for the method name
            String formattedMethodName = method.replace(".", "::");
            
            for (BlockInfo blockInfo : blocks) {
                // Run the Divine function for the block
                Diviner.Divine(RunID, formattedMethodName, blockInfo, Benchmark, iterations);
            
                // Generate block key in the same format as addEntry
                String blockKey = blockInfo.graalID + " (Vtune Block " + blockInfo.vtuneBlock + ")";
                
                // Retrieve and add regular slowdown data to GTBuildFinalSlowdownFile
                int slowdownValue = GTBuildSlowdownFile.slowdownData
                                        .getOrDefault(method.replace("::", "."), new HashMap<>())
                                        .getOrDefault(blockKey, 0);
                
                if (slowdownValue != 0) { // Add only if slowdown value exists
                    GTBuildFinalSlowdownFile.slowdownData
                        .computeIfAbsent(formattedMethodName.replace("::", "."), k -> new HashMap<>())
                        .put(blockKey, slowdownValue);
                }
    
                // Retrieve and add backend slowdown data to GTBuildFinalSlowdownFile if it exists
                int backendSlowdownValue = GTBuildSlowdownFile.backendSlowdownData
                                               .getOrDefault(method.replace("::", "."), new HashMap<>())
                                               .getOrDefault(blockKey, 0);
                
                if (backendSlowdownValue != 0) { // Only add if there is a backend slowdown value
                    GTBuildFinalSlowdownFile.backendSlowdownData
                        .computeIfAbsent(formattedMethodName.replace("::", "."), k -> new HashMap<>())
                        .put(blockKey, backendSlowdownValue);
                }
    
                // Clear the slowdown data after processing each block
                GTBuildSlowdownFile.slowdownData.clear();
                GTBuildSlowdownFile.backendSlowdownData.clear();
            }
    
            // Write final results for the method to a file
            GTBuildFinalSlowdownFile.writeToFile("Final_" + formattedMethodName.replace("::", "."), RunID);
        }
    }
    
}
