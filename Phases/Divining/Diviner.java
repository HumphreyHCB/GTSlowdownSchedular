package Phases.Divining;

import GTResources.AWFYBenchmarksLookUp;
import Phases.Divining.MarkerPhaseDataLookup.BlockInfo;
import VTune.VTuneAnalyzer;
import VTune.VTuneRunner;
import java.util.*;;

public class Diviner {

    // For a given method's block work out the correct amount of slowdown
    public static int Divine(String runID, String method, BlockInfo block, String Benchmark, int iterations) {

        double BaseCPUSpeed = block.baseCpuTime;
        double targetSpeed = BaseCPUSpeed * 2;

        int initialGuess = block.lineCount;
        int count = 0;

        int lowerBound = 1;
        int upperBound = initialGuess + 1; // Set initial bounds

        String Runlocation2 = VTuneRunner.runVtune(Benchmark, iterations,
        AWFYBenchmarksLookUp.getExtraArgs(Benchmark), false, false, "",
        runID + "_" + method.replace("::", "").replace(".", "") + "_" + block.vtuneBlock + "__" + "BaseRun");
        double basetotalCpuTime = VTuneAnalyzer.getTotalCpuTimeForMethodsBlocks(Runlocation2, method);

        while (upperBound - lowerBound > 1) {
            //Add slowdown entry and write to file for current initialGuess
            GTBuildSlowdownFile.addEntry(method, block.graalID, block.vtuneBlock, initialGuess, block.backendBlock);
            String pathtoSlowdownFile = GTBuildSlowdownFile
                    .writeToFile("_" + method + "_" + block.vtuneBlock + "_" + count, runID);

            // Run VTune analysis and get CPU speed for current initialGuess
            String Runlocation = VTuneRunner.runVtune(Benchmark, iterations,
                    AWFYBenchmarksLookUp.getExtraArgs(Benchmark), false, true, pathtoSlowdownFile,
                    runID + "_" + method.replace("::", "").replace(".", "") + "_" + block.vtuneBlock + "_" + count);
            double currentTotalCPUSpeed = VTuneAnalyzer.getTotalCpuTimeForMethodsBlocks(Runlocation, method);



            String outputFilePath = String.format("/home/hb478/repos/GTSlowdownSchedular/Data/%s/%s.txt",
                    runID + "_" + method.replace("::", "").replace(".", "") + "_" + block.vtuneBlock + "_" + count,
                    method.replaceAll("[\\/:*?\"<>|]", "_"));
            VTuneAnalyzer.generateMethodBlockVTuneReport(
                    runID + "_" + method.replace("::", "").replace(".", "") + "_" + block.vtuneBlock + "_" + count,
                    method, outputFilePath);

            if (currentTotalCPUSpeed > basetotalCpuTime + BaseCPUSpeed) {
                // If `currentCPUSpeed` exceeds target, set upperBound to initialGuess
                upperBound = initialGuess;
            } else {
                // If below target, set lowerBound to initialGuess
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
