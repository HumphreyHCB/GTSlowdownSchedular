package Phases.Marker;

import GTResources.AWFYBenchmarksLookUp;
import Phases.Common.BuildResultsForRun;
import VTune.VTuneAnalyzer;
import VTune.VTuneRunner;
import java.util.List;
import java.util.Map;

public class MarkerRunner {

    // this need to run the marker phase, then process the outputed result
    public static void run(String Benchmark, int iterations, String RunID, boolean compilerReplay) {

        String normalRunID = RunID + "_NormalRun";
        // Run the Vtune with nothing on
        VTuneRunner.runVtune(Benchmark, iterations, AWFYBenchmarksLookUp.getExtraArgs(Benchmark), false, false, "",
                normalRunID, compilerReplay);

        // Find all methods that Vtune Identifes
        Map<String, Double> normalRunfoundMethods = VTuneAnalyzer.getAllMethodsFoundByVtune(normalRunID);

        // get only the methods that are of significants
        List<String> normalRunsignificantMethods = MethodTargeter.findSignificantMethod(normalRunfoundMethods);

        BuildResultsForRun.run(normalRunID, normalRunsignificantMethods, true);

        String markerRunId = RunID + "_MarkerRun";
        // Run the Vtune with the Marker
        VTuneRunner.runVtune(Benchmark, iterations, AWFYBenchmarksLookUp.getExtraArgs(Benchmark), true, false, "",
                markerRunId, compilerReplay);


        // we should use a normal runs methods, as marking may schew the methods found that at signigficant

        BuildSlowdownTargetsList.run(markerRunId, normalRunsignificantMethods);

        if (!BuildMarkerPhaseInfo.build(RunID)) {
            // build failed, might be that there bad skid and alingenment is out, ill try
            // again
            System.out.println("Building again");
            if (!BuildMarkerPhaseInfo.build(RunID)) {
                System.out.println("Build failed for a second time, ending");
                System.exit(-1);
            }

        }

    }

}
