package Phases.Marker;

import java.util.List;
import java.util.Map;

import GTResources.AWFYBenchmarksLookUp;
import VTune.VTuneAnalyzer;
import VTune.VTuneReportRipper;
import VTune.VTuneRunner;

public class MarkerRunner {
    
    // this need to run the marker phase, then process the outputed result
    public static void run(String Benchmark, int iterations, String RunID) {

        String normalRunID =  RunID+"_NormalRun";
        // Run the Vtune with nothing on
        VTuneRunner.runVtune(Benchmark,iterations, AWFYBenchmarksLookUp.getExtraArgs(Benchmark), false, false, "" , normalRunID); 

       // Find all methods that Vtune Identifes
       Map<String, Double> foundMethods = VTuneAnalyzer.getAllMethodsFoundByVtune(normalRunID);

       // get only the methods that are of significants
       List<String> significantMethods =  MethodTargeter.findSignificantMethod(foundMethods);


       String markerRunId =  RunID+"_MarkerRun";
        // Run the Vtune with the Marker
        VTuneRunner.runVtune(Benchmark,iterations, AWFYBenchmarksLookUp.getExtraArgs(Benchmark), true, false, "" , markerRunId); 
       
        BuildSlowdownTargetsList.run(markerRunId,  significantMethods);



    }

}
