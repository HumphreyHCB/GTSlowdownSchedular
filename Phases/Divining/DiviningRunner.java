package Phases.Divining;

import java.util.List;

import Phases.Divining.MarkerPhaseDataLookup.BlockInfo;

public class DiviningRunner {
    
    public static void run(String Benchmark, int iterations, String RunID) {


        // load the data from the Marker Phase into static memeory
        MarkerPhaseDataLookup.loadData(RunID);

        // get a list of all the methods we will be "attacking"
        List<String> methods = MarkerPhaseDataLookup.getAllMethods();


        // atm just Queens::getRowColumn 

        List<BlockInfo> blocks = MarkerPhaseDataLookup.getBenchmarkEntries("Queens::getRowColumn");

        for (BlockInfo blockInfo : blocks) {
            Diviner.Divine(RunID, blockInfo, Benchmark, iterations);
        }






    }

}
