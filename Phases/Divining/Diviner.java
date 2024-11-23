package Phases.Divining;

import GTResources.AWFYBenchmarksLookUp;
import Phases.Common.RemoveVtuneRun;
import Phases.Divining.MarkerPhaseDataLookup.BlockInfo;
import VTune.VTuneAnalyzer;
import VTune.VTuneRunner;

import java.io.File;
import java.util.*;;

public class Diviner {

    // For a given method's block work out the correct amount of slowdown
    public static int DivineDontUse(String runID, String method, BlockInfo block, String Benchmark, int iterations,
            Boolean lowFootPrint) {

        if (block.baseCpuTime < 0.09) {
            return 0; // not worth our time just skip
        }

        double baseCPUSpeed = block.baseCpuTime;
        double targetSpeed = baseCPUSpeed * 2;

        int initialGuess = 1;
        int count = 0;
        int lowerBound = 1;
        int upperBound = initialGuess + 2; // Set initial bounds, we get at least 1 loop

        while (upperBound - lowerBound > 1) {
            // Add slowdown entry and write to file for current initialGuess
            GTBuildSlowdownFile.addEntry(method, block.graalID, block.vtuneBlock, initialGuess, block.backendBlock);
            String pathToSlowdownFile = GTBuildSlowdownFile
                    .writeToFile("_" + method + "_" + block.vtuneBlock + "_" + count, runID);

            // Run VTune analysis and get CPU speed for current initialGuess
            String formattedRunLocation = runID + "_" + method.replace("::", "").replace(".", "") + "_"
                    + block.vtuneBlock + "_" + count;
            String runLocation = VTuneRunner.runVtune(Benchmark, iterations,
                    AWFYBenchmarksLookUp.getExtraArgs(Benchmark), false, true, pathToSlowdownFile,
                    formattedRunLocation);
            double currentBlockCPUSpeed = VTuneAnalyzer.getCpuTimeForBlock(runLocation, method, block.vtuneBlock);

            if (lowFootPrint) {
                String directoryPath = "/home/hb478/repos/GTSlowdownSchedular/Data/" + runID
                        + "_SlowDown_Data/LowFootPrintDumps";
                File directory = new File(directoryPath);

                // Create the directory if it does not exist
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                String outputFilePath2 = String.format("/home/hb478/repos/GTSlowdownSchedular/Data/%s/%s.txt",
                        runID + "_SlowDown_Data/LowFootPrintDumps/", formattedRunLocation);
                VTuneAnalyzer.generateMethodBlockVTuneReport(formattedRunLocation, method, outputFilePath2);
                RemoveVtuneRun.run(runLocation);
            }

            // Adjust bounds based on the current CPU speed
            if (currentBlockCPUSpeed > targetSpeed) {
                upperBound = initialGuess;
            } else {
                lowerBound = initialGuess;
                upperBound = Math.max(upperBound, initialGuess + 2); // Dynamically expand upperBound if needed
            }

            // Calculate new guess as the midpoint of current bounds
            initialGuess = (lowerBound + upperBound) / 2;
            count++;
        }

        return upperBound;

    }

    // For a given method's block work out the correct amount of slowdown
    public static int DivineSimple(String runID, String method, BlockInfo block, String Benchmark, int iterations,
            Boolean lowFootPrint) {

        if (block.baseCpuTime < 0.09) {
            return 0; // not worth our time just skip
        }

        double baseCPUSpeed = block.baseCpuTime;
        double targetSpeed = baseCPUSpeed * 2;

        int initialGuess = 1;
        int count = 0;

        while (true) {
            // Add slowdown entry and write to file for current initialGuess
            GTBuildSlowdownFile.addEntry(method, block.graalID, block.vtuneBlock, initialGuess, block.backendBlock);
            String pathToSlowdownFile = GTBuildSlowdownFile
                    .writeToFile("_" + method + "_" + block.vtuneBlock + "_" + count, runID);

            // Run VTune analysis and get CPU speed for current initialGuess
            String formattedRunLocation = runID + "_" + method.replace("::", "").replace(".", "") + "_"
                    + block.vtuneBlock + "_" + count;
            String runLocation = VTuneRunner.runVtune(Benchmark, iterations,
                    AWFYBenchmarksLookUp.getExtraArgs(Benchmark), false, true, pathToSlowdownFile,
                    formattedRunLocation);
            double currentBlockCPUSpeed = VTuneAnalyzer.getCpuTimeForBlock(runLocation, method, block.vtuneBlock);

            if (lowFootPrint) {
                String directoryPath = "/home/hb478/repos/GTSlowdownSchedular/Data/" + runID
                        + "_SlowDown_Data/LowFootPrintDumps";
                File directory = new File(directoryPath);

                // Create the directory if it does not exist
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                String outputFilePath2 = String.format("/home/hb478/repos/GTSlowdownSchedular/Data/%s/%s.txt",
                        runID + "_SlowDown_Data/LowFootPrintDumps/", formattedRunLocation);
                VTuneAnalyzer.generateMethodBlockVTuneReport(formattedRunLocation, method, outputFilePath2);
                RemoveVtuneRun.run(runLocation);
            }

            // Check if the current CPU speed meets or exceeds the target speed
            if (currentBlockCPUSpeed > targetSpeed) {
                break;
            }

            // Increment the slowdown and repeat
            initialGuess++;
            count++;
        }

        return initialGuess - 1; // atm we find an under estimate is a better value
    }

    public static int DivineComplex(String runID, String method, BlockInfo block,
            String Benchmark,
            int iterations, Boolean lowFootPrint) {

        if (block.baseCpuTime < 0.09) {
            return 0; // not worth our time, just skip
        }

        double baseCPUSpeed = block.baseCpuTime;
        double targetSpeed = baseCPUSpeed * 2;

        int guess = 1;
        int closestUnder = -1;
        int closestOver = -1;

        // Step 1: Increment by 3 until overshooting the target
        while (true) {
            // Add slowdown entry and write to file for the current guess
            GTBuildSlowdownFile.addEntry(method, block.graalID, block.vtuneBlock, guess, block.backendBlock);
            String pathToSlowdownFile = GTBuildSlowdownFile
                    .writeToFile("_" + method + "_" + block.vtuneBlock + "_" + guess, runID);

            // Run VTune analysis and get CPU speed for the current guess
            String formattedRunLocation = runID + "_" + method.replace("::", "").replace(".", "") + "_"
                    + block.vtuneBlock + "_" + guess;
            String runLocation = VTuneRunner.runVtune(Benchmark, iterations,
                    AWFYBenchmarksLookUp.getExtraArgs(Benchmark), false, true, pathToSlowdownFile,
                    formattedRunLocation);
            double currentBlockCPUSpeed = VTuneAnalyzer.getCpuTimeForBlock(runLocation, method, block.vtuneBlock);

            if (lowFootPrint) {
                String directoryPath = "/home/hb478/repos/GTSlowdownSchedular/Data/" + runID
                        + "_SlowDown_Data/LowFootPrintDumps";
                File directory = new File(directoryPath);

                // Create the directory if it does not exist
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                String outputFilePath2 = String.format("/home/hb478/repos/GTSlowdownSchedular/Data/%s/%s.txt",
                        runID + "_SlowDown_Data/LowFootPrintDumps/", formattedRunLocation);
                VTuneAnalyzer.generateMethodBlockVTuneReport(formattedRunLocation, method, outputFilePath2);
                RemoveVtuneRun.run(runLocation);
            }

            if (currentBlockCPUSpeed > targetSpeed) {
                // We've gone over the target, break out of the loop
                closestOver = guess;
                break;
            } else {
                // Update closestUnder and increment the guess
                closestUnder = guess;
                guess += 4;
                if (guess > 100) {
                    guess += 50;
                }
            }
        }

        // Step 2: Fine-tune to find the exact number that makes it go over
        guess = closestUnder + 1 ; // Start just above the last "under" value
        while (true) {
            // Add slowdown entry and write to file for the current guess
            GTBuildSlowdownFile.addEntry(method, block.graalID, block.vtuneBlock, guess, block.backendBlock);
            String pathToSlowdownFile = GTBuildSlowdownFile
                    .writeToFile("_" + method + "_" + block.vtuneBlock + "_" + guess, runID);

            // Run VTune analysis and get CPU speed for the current guess
            String formattedRunLocation = runID + "_" + method.replace("::", "").replace(".", "") + "_"
                    + block.vtuneBlock + "_" + guess;
            String runLocation = VTuneRunner.runVtune(Benchmark, iterations,
                    AWFYBenchmarksLookUp.getExtraArgs(Benchmark), false, true, pathToSlowdownFile,
                    formattedRunLocation);
            double currentBlockCPUSpeed = VTuneAnalyzer.getCpuTimeForBlock(runLocation, method, block.vtuneBlock);

            if (lowFootPrint) {
                // Handle low footprint option: generate report and remove VTune run
                String directoryPath = "/home/hb478/repos/GTSlowdownSchedular/Data/" + runID
                        + "_SlowDown_Data/LowFootPrintDumps";
                File directory = new File(directoryPath);
    
                // Create the directory if it does not exist
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                String outputFilePath2 = String.format("/home/hb478/repos/GTSlowdownSchedular/Data/%s/%s.txt",
                        runID + "_SlowDown_Data/LowFootPrintDumps/", formattedRunLocation);
                VTuneAnalyzer.generateMethodBlockVTuneReport(formattedRunLocation, method, outputFilePath2);
                RemoveVtuneRun.run(runLocation);
            }

            if (currentBlockCPUSpeed > targetSpeed) {
                // guess is now first over
                break;
            } else {
                guess++;
            }

            if (guess > closestOver) {
                guess = closestOver;
                break;
            }
        }

        return guess; // Return both values
    }
}
