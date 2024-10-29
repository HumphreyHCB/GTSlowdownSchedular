package Phases.Divining;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import Phases.Divining.MarkerPhaseDataLookup.BlockInfo;

public class DiviningRunner {
    
    public static void run(String Benchmark, int iterations, String RunID) {


        // load the data from the Marker Phase into static memeory
        MarkerPhaseDataLookup.loadData(RunID);

        // get a list of all the methods we will be "attacking"
        List<String> methods = MarkerPhaseDataLookup.getAllMethods();
        for (String method : methods) {
            
        

        // atm just Queens::getRowColumn 

        List<BlockInfo> blocks = MarkerPhaseDataLookup.getBenchmarkEntries(method.replace(".", "::"));

        Map<String, Map<String, Integer>> MethodsSlowdownData = new HashMap<>();

        for (BlockInfo blockInfo : blocks) {
            Diviner.Divine(RunID, method.replace(".", "::"), blockInfo, Benchmark, iterations);
            
            // Generate method and block key in the same format as addEntry
            String formattedMethodName = method.replace(".", "::");  // Convert "::" to "."
            String blockKey = blockInfo.graalID + " (Vtune Block " + blockInfo.vtuneBlock + ")";
        
            // After divining, save the current slowdown data for this block
            MethodsSlowdownData
                    .computeIfAbsent(formattedMethodName, k -> new HashMap<>())
                    .put(blockKey, GTBuildSlowdownFile.slowdownData.getOrDefault(formattedMethodName, new HashMap<>()).getOrDefault(blockKey, 0));
            
            // Clear the slowdown data for the next iteration
            GTBuildSlowdownFile.slowdownData.clear();
        }
        
        // Reconstruct the slowdown data from all blocks and write to the final file
        for (Map.Entry<String, Map<String, Integer>> entry : MethodsSlowdownData.entrySet()) {
            GTBuildSlowdownFile.slowdownData.computeIfAbsent(entry.getKey(), k -> new HashMap<>()).putAll(entry.getValue());
        }
        GTBuildSlowdownFile.writeToFile("Final_"+method, RunID);
        
    }





    }

}
