package Phases.Divining;

import GTResources.AWFYBenchmarksLookUp;
import Phases.Divining.MarkerPhaseDataLookup.BlockInfo;
import VTune.VTuneAnalyzer;
import VTune.VTuneRunner;

public class Diviner {

    // For a given method's block work out the correct amount of slowdown
    public static int Divine(String runID, String method ,BlockInfo block, String Benchmark, int iterations) {
        
        double BaseCPUSpeed = block.baseCpuTime;

        //int initialGuess = block.lineCount;
        int initialGuess = 0;
        int count = 0;

        boolean looking = true;
        while (looking) {
            // Add initial slowdown
            GTBuildSlowdownFile.addEntry(method, block.graalID, block.vtuneBlock, initialGuess);
            String pathtoSlowdownFile = GTBuildSlowdownFile.writeToFile("_"+method+"_" + block.vtuneBlock + "_" + count, runID);
    
            // Run VTune and analyze the results
            String Runlocation = VTuneRunner.runVtune(Benchmark, iterations, AWFYBenchmarksLookUp.getExtraArgs(Benchmark), false, true, pathtoSlowdownFile, runID+ "_"+method.replace("::", "").replace(".", "")+"_"+ block.vtuneBlock + "_" + count);
            double currentCPUSpeed = VTuneAnalyzer.getCpuTimeForBlock(Runlocation, method, block.vtuneBlock);
    
            System.out.println("Before slowdown");
            System.out.println(BaseCPUSpeed);
    
            System.out.println("After slowdown");
            System.out.println(currentCPUSpeed);
    
            // Check if current CPU speed is just over 100% of BaseCPUSpeed
            if (currentCPUSpeed >= BaseCPUSpeed) {
                looking = false;
            } else {
                // Increase initial guess to try and reach the target
                initialGuess += 1;
            }
    
            count++;
        }

        return initialGuess;




    }


}
