package Phases.Divining;

import GTResources.AWFYBenchmarksLookUp;
import Phases.Divining.MarkerPhaseDataLookup.BlockInfo;
import VTune.VTuneAnalyzer;
import VTune.VTuneRunner;
import java.util.*;;

public class Diviner {

    // For a given method's block work out the correct amount of slowdown
    public static int Divine(String runID, String method, BlockInfo block, String Benchmark, int iterations) {
        double baseCPUSpeed = block.baseCpuTime;
        double targetSpeed = baseCPUSpeed * 2;
    
        int initialGuess = block.lineCount;
        int count = 0;
        int lowerBound = 1;
        int upperBound = initialGuess + 2; // Set initial bounds, we get at least 1 loop
    
        // Run VTune to get the base total CPU time
        // String formattedRunID = runID + "_" + method.replace("::", "").replace(".", "") + "_" + block.vtuneBlock + "__BaseRun";
        // String runLocationBase = VTuneRunner.runVtune(Benchmark, iterations, AWFYBenchmarksLookUp.getExtraArgs(Benchmark), false, false, "", formattedRunID);
        // double baseTotalCpuTime = VTuneAnalyzer.getTotalCpuTimeForMethodsBlocks(runLocationBase, method);

        while (upperBound - lowerBound > 1) {
            // Add slowdown entry and write to file for current initialGuess
            GTBuildSlowdownFile.addEntry(method, block.graalID, block.vtuneBlock, initialGuess, block.backendBlock);
            String pathToSlowdownFile = GTBuildSlowdownFile.writeToFile("_" + method + "_" + block.vtuneBlock + "_" + count, runID);
    
            // Run VTune analysis and get CPU speed for current initialGuess
            String formattedRunLocation = runID + "_" + method.replace("::", "").replace(".", "") + "_" + block.vtuneBlock + "_" + count;
            String runLocation = VTuneRunner.runVtune(Benchmark, iterations, AWFYBenchmarksLookUp.getExtraArgs(Benchmark), false, true, pathToSlowdownFile, formattedRunLocation);
            double currentTotalCPUSpeed = VTuneAnalyzer.getCpuTimeForBlock(runLocation, method, block.vtuneBlock);
    
            // Generate VTune report
            String outputFilePath = String.format("/home/hb478/repos/GTSlowdownSchedular/Data/%s/%s.txt", formattedRunLocation, method.replaceAll("[\\/:*?\"<>|]", "_"));
            VTuneAnalyzer.generateMethodBlockVTuneReport(formattedRunLocation, method, outputFilePath);
    
            // Adjust bounds based on the current CPU speed
            if (currentTotalCPUSpeed > targetSpeed) {
                upperBound = initialGuess;
            } else {
                lowerBound = initialGuess;
                upperBound = Math.max(upperBound, initialGuess * 2); // Dynamically expand upperBound if needed
            }
    
            // Calculate new guess as the midpoint of current bounds
            initialGuess = (lowerBound + upperBound) / 2;
            count++;
        }
    
        return lowerBound;

        // At this point, upperBound should be the minimal value exceeding the target.
        // Confirm by testing lowerBound.
        // Verify that lowerBound does not meet the target, while upperBound does.
        // GTBuildSlowdownFile.addEntry(method, block.graalID, block.vtuneBlock, lowerBound, block.backendBlock);
        // String lowerPath = GTBuildSlowdownFile
        //         .writeToFile("_" + method + "_" + block.vtuneBlock + "_" + count + "_lower", runID);
        // double lowerCPUSpeed = VTuneAnalyzer.getCpuTimeForBlock(VTuneRunner.runVtune(Benchmark, iterations,
        //         AWFYBenchmarksLookUp.getExtraArgs(Benchmark), false, true, lowerPath, runID + "_lower"), method,
        //         block.vtuneBlock);

        // if (lowerCPUSpeed > basetotalCpuTime + targetSpeed) {
        //     return lowerBound; // If `lowerBound` already exceeds, it’s the minimal slowdown
        // } else {
        //     return upperBound; // Otherwise, `upperBound` is the first to exceed
        // }
    }

}
