package Phases.Divining;

import GTResources.AWFYBenchmarksLookUp;
import Phases.Divining.MarkerPhaseDataLookup.BlockInfo;
import VTune.VTuneAnalyzer;
import VTune.VTuneRunner;

public class Diviner {

    // For a given method's block work out the correct amount of slowdown
    public static void Divine(String runID, String method ,BlockInfo block, String Benchmark, int iterations) {
        
        double BaseCPUSpeed = block.baseCpuTime;

        int initialGuess = block.lineCount;
        initialGuess = 20;
        int count = 0;

        GTBuildSlowdownFile.addEntry(method,Integer.parseInt(block.graalID), Integer.parseInt(block.vtuneBlock), initialGuess);

        String pathtoSlowdownFile = GTBuildSlowdownFile.writeToFile("" + block.vtuneBlock+ "_"+ count, runID);

        String Runlocation = VTuneRunner.runVtune(Benchmark, iterations, AWFYBenchmarksLookUp.getExtraArgs(Benchmark), false, true, pathtoSlowdownFile, runID);    

        System.out.println("Before slowdown");
        System.out.println(BaseCPUSpeed);

        System.out.println("After slowdown");
        System.out.println(VTuneAnalyzer.getCpuTimeForBlock(Runlocation, method, Integer.parseInt(block.vtuneBlock)));




    }


}
