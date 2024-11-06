package Phases.Divining;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import Phases.Divining.MarkerPhaseDataLookup.BlockInfo;

public class DiviningRunner {
    
    public static void run(String Benchmark, int iterations, String RunID, Boolean lowFootPrint) {
        // Load the data from the Marker Phase into static memory
        MarkerPhaseDataLookup.loadData(RunID);
    
        // Get a list of all the methods we will be "attacking"
        List<String> methods = MarkerPhaseDataLookup.getAllMethods();
        
        for (String method : methods) {
            // Clear final data at the start of each method
            GTBuildFinalSlowdownFile.slowdownData.clear();
            GTBuildFinalSlowdownFile.backendSlowdownData.clear();
            
            List<BlockInfo> blocks = MarkerPhaseDataLookup.getBenchmarkEntries(method.replace(".", "::"));
            
            // Use a consistent format for the method name
            String formattedMethodName = method.replace(".", "::");
            
            for (BlockInfo blockInfo : blocks) {
                // Run the Divine function for the block and capture the returned slowdown value
                int slowdownValue = Diviner.Divine(RunID, formattedMethodName, blockInfo, Benchmark, iterations, lowFootPrint);
                //int slowdownValue = 1;
                // Generate block key in the same format as addEntry
                String blockKey = blockInfo.graalID + " (Vtune Block " + blockInfo.vtuneBlock + ")";
                
                // Check if it's a backend block or regular block and store accordingly
                if (blockInfo.backendBlock) { // Assuming BlockInfo has a field to denote backend
                    GTBuildFinalSlowdownFile.backendSlowdownData
                        .computeIfAbsent(formattedMethodName.replace("::", "."), k -> new HashMap<>())
                        .put(blockKey, slowdownValue);
                } else {
                    GTBuildFinalSlowdownFile.slowdownData
                        .computeIfAbsent(formattedMethodName.replace("::", "."), k -> new HashMap<>())
                        .put(blockKey, slowdownValue);
                }

                GTBuildSlowdownFile.slowdownData.clear();
                GTBuildSlowdownFile.backendSlowdownData.clear();
            }
            
            // Write final results for the method to a file
            GTBuildFinalSlowdownFile.writeToFile("Final_" + formattedMethodName.replace("::", "."), RunID);
            
            // Clear final data specific to the method after writing
            GTBuildFinalSlowdownFile.slowdownData.clear();
            GTBuildFinalSlowdownFile.backendSlowdownData.clear();
        }
    }
    
}

