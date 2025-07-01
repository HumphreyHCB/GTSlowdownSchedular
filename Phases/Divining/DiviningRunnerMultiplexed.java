package Phases.Divining;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import GTResources.AWFYBenchmarksLookUp;
import Phases.Common.RemoveVtuneRun;
import Phases.Common.SlowdownFileRetriever;
import Phases.Divining.MarkerPhaseDataLookup.BlockInfo;
import VTune.VTuneAnalyzer;
import VTune.VTuneRunner;

public class DiviningRunnerMultiplexed {

    public static void run(String Benchmark, int iterations, String RunID, Boolean lowFootPrint, boolean compilerReplay,
            double slowdownAmount) {
        // Load the data from the Marker Phase into static memory
        MarkerPhaseDataLookup.loadData(RunID);

        // Get a list of all the methods we will be "attacking"
        List<String> methods = MarkerPhaseDataLookup.getAllMethods();

        for (String method : methods) {
            GTBuildFinalSlowdownFile.slowdownData.clear();
            GTBuildFinalSlowdownFile.backendSlowdownData.clear();

            List<BlockInfo> blocks = MarkerPhaseDataLookup.getBenchmarkEntries(method.replace(".", "::"));
            String formattedMethodName = method.replace(".", "::");

            // Keep track of blocks that are still being adjusted
            Map<BlockInfo, Integer> activeBlocks = new HashMap<>();
            for (BlockInfo blockInfo : blocks) {
                if (blockInfo.baseCpuTime < 0.0001) {
                    // blocks like these are too unstable, and might not aprea in all ones with no
                    // time
                } else {
                    activeBlocks.put(blockInfo, 1); // Start each block with an initial slowdown guess of 1
                }
            }

            boolean allBlocksCompleted = false;
            int iterationCounter = 0; // Iteration counter for file names

            while (!allBlocksCompleted) {
                allBlocksCompleted = true; // Assume all blocks are completed until proven otherwise
                iterationCounter++; // Increment the counter for each iteration

                // Write slowdown values for all blocks to a single file
                for (Map.Entry<BlockInfo, Integer> entry : activeBlocks.entrySet()) {
                    BlockInfo block = entry.getKey();
                    int currentSlowdown = entry.getValue();

                    // Add the current slowdown entry to the file
                    GTBuildSlowdownFile.addEntry(formattedMethodName, block.graalID, block.vtuneBlock, currentSlowdown,
                            block.backendBlock);
                }

                // Write the combined slowdown file for all blocks
                String pathToSlowdownFile = GTBuildSlowdownFile.writeToFile(
                        "_Iter" + iterationCounter + "_" + formattedMethodName,
                        RunID);

                // Generate a unique RunID for VTune using the iteration counter
                String uniqueRunID = RunID + "_Iter" + iterationCounter;

                // Perform a single VTune run for all blocks
                String runLocation = VTuneRunner.runVtune(
                        Benchmark,
                        iterations,
                        AWFYBenchmarksLookUp.getExtraArgs(Benchmark),
                        false,
                        true,
                        pathToSlowdownFile,
                        uniqueRunID, // Pass the unique RunID
                        compilerReplay);

                // Check results for each block

                Map<Integer, Double> blocksSpeeds = VTuneAnalyzer.getCpuTimesForAllBlocks(runLocation,
                        formattedMethodName);
                int BlocksThatAreDone = 0;
                BlockInfo lastblock = null;
                for (Map.Entry<BlockInfo, Integer> entry : activeBlocks.entrySet()) {

                    BlockInfo block = entry.getKey();
                    double currentBlockCPUSpeed = blocksSpeeds.getOrDefault(block.vtuneBlock, 10000.0); // Default to a
                                                                                                        // high value if
                                                                                                        // the block is
                                                                                                        // not found

                    // Check if the block has reached its target speed
                    if (currentBlockCPUSpeed < block.baseCpuTime * slowdownAmount) {
                        entry.setValue(entry.getValue() + 1); // Increment slowdown for the block
                        if (entry.getValue() > 500) {
                            entry.setValue(entry.getValue() + 100); // Increment slowdown for the block again (faster
                                                                    // Divining)
                        } else if (entry.getValue() > 200) {
                            entry.setValue(entry.getValue() + 50); // Increment slowdown for the block again (faster
                                                                   // Divining)
                        } else if (entry.getValue() > 100) {
                            entry.setValue(entry.getValue() + 10); // Increment slowdown for the block again (faster
                                                                   // Divining)
                        } else if (entry.getValue() > 50) {
                            entry.setValue(entry.getValue() + 5); // Increment slowdown for the block again (faster
                                                                  // Divining)
                        } else if (entry.getValue() + 1 > 20) {
                            entry.setValue(entry.getValue() + 1); // Increment slowdown for the block again (faster
                                                                  // Divining)
                        }

                        allBlocksCompleted = false; // At least one block is still active
                        lastblock = block;
                    } else {
                        BlocksThatAreDone++;
                        int finalSlowdownValue = entry.getValue() - 1; // Set to one less than the current value
                        // Save the slowdown result for the completed block
                        String blockKey = block.graalID + " (Vtune Block " + block.vtuneBlock + ")";
                        if (block.backendBlock) {
                            GTBuildFinalSlowdownFile.backendSlowdownData
                                    .computeIfAbsent(formattedMethodName.replace("::", "."), k -> new HashMap<>())
                                    .put(blockKey, finalSlowdownValue);
                        } else {
                            GTBuildFinalSlowdownFile.slowdownData
                                    .computeIfAbsent(formattedMethodName.replace("::", "."), k -> new HashMap<>())
                                    .put(blockKey, finalSlowdownValue);
                        }
                    }

                }

                // so if we are on the very last block, we run the more refined diviner
                if (BlocksThatAreDone == activeBlocks.size() - 1) {
                    int lastGeuss = activeBlocks.get(lastblock);

                    if (activeBlocks.get(lastblock) > 500) {
                        lastGeuss = lastGeuss - 100;
                    } else if (activeBlocks.get(lastblock) > 200) {
                        lastGeuss = lastGeuss - 50;
                    } else if (activeBlocks.get(lastblock) > 100) {
                        lastGeuss = lastGeuss - 10;
                    } else if (activeBlocks.get(lastblock) > 50) {
                        lastGeuss = lastGeuss - 5;
                    } else if (activeBlocks.get(lastblock) > 20) {
                        lastGeuss = lastGeuss - 1;
                    }

                    int slowdown = Diviner.DivineComplex(RunID, formattedMethodName, lastblock, Benchmark, iterations,
                            lowFootPrint, compilerReplay, slowdownAmount, activeBlocks.get(lastblock), lastGeuss);
                    String blockKey = lastblock.graalID + " (Vtune Block " + lastblock.vtuneBlock + ")";
                    if (lastblock.backendBlock) {
                        GTBuildFinalSlowdownFile.backendSlowdownData
                                .computeIfAbsent(formattedMethodName.replace("::", "."), k -> new HashMap<>())
                                .put(blockKey, slowdown);
                    } else {
                        GTBuildFinalSlowdownFile.slowdownData
                                .computeIfAbsent(formattedMethodName.replace("::", "."), k -> new HashMap<>())
                                .put(blockKey, slowdown);
                    }

                    allBlocksCompleted = true;

                }

                if (lowFootPrint) {
                    String directoryPath = "/home/hb478/repos/GTSlowdownSchedular/Data/" + RunID
                            + "_SlowDown_Data/LowFootPrintDumps";
                    File directory = new File(directoryPath);

                    // Create the directory if it does not exist
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }

                    // Extract the last part of runLocation to ensure it's treated as a file name
                    String fileName = new File(runLocation).getName();

                    // Construct the output file path correctly
                    String outputFilePath2 = directoryPath + "/" + fileName + "_" + method + ".txt";

                    // Generate the VTune report
                    VTuneAnalyzer.generateMethodBlockVTuneReport(fileName, method, outputFilePath2);

                    // Clean up VTune run
                    RemoveVtuneRun.run(runLocation);
                }

                // Clear temporary data structures after each iteration
                GTBuildSlowdownFile.slowdownData.clear();
                GTBuildSlowdownFile.backendSlowdownData.clear();
            }

            // Write final results for the method to a file
            GTBuildFinalSlowdownFile.writeToFile("Final_" + formattedMethodName.replace("::", "."), RunID);
        }

    }

    public static void runComplex(
            String Benchmark,
            int iterations,
            String RunID,
            boolean lowFootPrint,
            boolean compilerReplay,
            double slowdownAmount) {
        // 1. Load marker-phase data
        MarkerPhaseDataLookup.loadData(RunID);

        // 2. Gather all methods
        List<String> methods = MarkerPhaseDataLookup.getAllMethods();

        // 3. For each method, do the new "complex" logic
        for (String method : methods) {

            // Clean up final slowdown data for each iteration of this method
            GTBuildFinalSlowdownFile.slowdownData.clear();
            GTBuildFinalSlowdownFile.backendSlowdownData.clear();

            GTBuildSlowdownFile.slowdownData.clear();
            GTBuildSlowdownFile.backendSlowdownData.clear();

            // Format the method name as needed for usage elsewhere
            String formattedMethodName = method.replace(".", "::");

            // 3.1 Get all block infos for this method
            List<BlockInfo> blocks = MarkerPhaseDataLookup.getBenchmarkEntries(formattedMethodName);

            // 3.2 Create a tracker for each block, if it’s not trivially small
            // and start each with slowdown guess = 1
            Map<BlockInfo, BlockSlowdownTracker> trackers = new HashMap<>();
            Map<BlockInfo, Integer> slowdownGuesses = new HashMap<>();

            for (BlockInfo blockInfo : blocks) {
                if (blockInfo.baseCpuTime < 0.0001) {
                    // Skip extremely small / negligible blocks
                    continue;
                }
                // Initialize with slowdown=1
                slowdownGuesses.put(blockInfo, 1);

                // Create a tracker using the new class
                BlockSlowdownTracker tracker = new BlockSlowdownTracker(blockInfo, slowdownAmount);
                // We also want to record an initial "measurement" of 0 time, just to set it up
                // or we can leave it until after the first actual measurement
                trackers.put(blockInfo, tracker);
            }

            if (trackers.isEmpty()) {
                // If no valid blocks, just skip
                continue;
            }

            // 3.3 We'll iterate until all blocks are done
            boolean allBlocksCompleted = false;
            int iterationCounter = 0;

            while (!allBlocksCompleted) {
                allBlocksCompleted = true; // assume done until proven otherwise
                iterationCounter++;

                for (Map.Entry<BlockInfo, BlockSlowdownTracker> entry : trackers.entrySet()) {
                    BlockInfo block = entry.getKey();
                    BlockSlowdownTracker tracker = entry.getValue();

                    if (!tracker.isDone()) {
                        // Retrieve or set the slowdown guess
                        int currentGuess = slowdownGuesses.getOrDefault(block, 1);

                        // Write this entry to the slowdown file
                        GTBuildSlowdownFile.addEntry(
                                formattedMethodName,
                                block.graalID,
                                block.vtuneBlock,
                                currentGuess,
                                block.backendBlock);

                        // Mark that we are not done if at least one block is not done
                        allBlocksCompleted = false;
                    }
                }

                // If everything was done before we even run, break the loop
                if (allBlocksCompleted) {
                    break;
                }

                // 3.3.2 write out the slowdown file to disk
                String pathToSlowdownFile = GTBuildSlowdownFile.writeToFile(
                        "_Iter" + iterationCounter + "_" + formattedMethodName,
                        RunID);

                // 3.3.3 Run VTune with these slowdowns
                String uniqueRunID = RunID + "_Iter" + iterationCounter;
                String runLocation = VTuneRunner.runVtune(
                        Benchmark,
                        iterations,
                        AWFYBenchmarksLookUp.getExtraArgs(Benchmark),
                        false,
                        true,
                        pathToSlowdownFile,
                        uniqueRunID,
                        compilerReplay);

                // 3.3.4 Get the CPU times from VTune
                Map<Integer, Double> blocksSpeeds = VTuneAnalyzer.getCpuTimesForAllBlocks(runLocation,
                        formattedMethodName);

                // 3.3.5 For each block that is not done, record measurement & see if we
                // overshot
                int blocksDoneCount = 0;
                BlockInfo lastActiveBlock = null;

                for (Map.Entry<BlockInfo, BlockSlowdownTracker> entry : trackers.entrySet()) {
                    BlockInfo block = entry.getKey();
                    BlockSlowdownTracker tracker = entry.getValue();

                    if (tracker.isDone()) {
                        // already finalized
                        blocksDoneCount++;
                        continue;
                    }

                    // retrieve current guess
                    int currentGuess = slowdownGuesses.get(block);

                    // read measured CPU time from the dictionary (or a default)
                    double measuredTime = blocksSpeeds.getOrDefault(block.vtuneBlock, 9999.0);

                    // record the measurement in the tracker
                    // (this call sets 'last' to what 'current' was,
                    // and 'current' to the new measurement)
                    tracker.recordMeasurement(currentGuess, measuredTime);

                    // see if we overshot enough to finalize
                    Integer maybeFinal = tracker.determineBestSlowdownIfOvershot();
                    if (maybeFinal != null) {
                        // If we just got a final slowdown, store it
                        storeFinalSlowdown(
                                formattedMethodName.replace("::", "."),
                                block,
                                maybeFinal);
                        blocksDoneCount++;
                    } else {
                        // still under target => we may need to increment
                        if (tracker.isUnderTarget()) {
                            // increment slowdown
                            int newGuess = currentGuess + 1;

                            // We can also replicate your "faster increment" logic:
                            if (currentGuess > 500) {
                                newGuess += 100;
                            } else if (currentGuess > 200) {
                                newGuess += 50;
                            } else if (currentGuess > 100) {
                                newGuess += 10;
                            } else if (currentGuess > 50) {
                                newGuess += 5;
                            } else if (currentGuess > 20) {
                                newGuess += 1;
                            }

                            slowdownGuesses.put(block, newGuess);
                        } else {
                            // If it's neither under target nor overshot,
                            // it might already be in the sweet spot.
                            // This can happen if measuredTime is extremely close
                            // to targetTime or exactly equals it.
                            // Mark as final if you want, or keep iterating:
                            tracker.isDone = true;
                            storeFinalSlowdown(
                                    formattedMethodName.replace("::", "."),
                                    block,
                                    currentGuess);
                            blocksDoneCount++;
                        }
                    }

                    lastActiveBlock = tracker.isDone() ? lastActiveBlock : block;
                }

                // 3.3.6 Check if we only have one block left, refine with Diviner
                // (like your original code)
                int totalBlocks = trackers.size();
                if (blocksDoneCount == totalBlocks - 1) {
                    // Identify the last not-done block
                    BlockInfo notDoneBlock = null;
                    for (BlockInfo b : trackers.keySet()) {
                        if (!trackers.get(b).isDone()) {
                            notDoneBlock = b;
                            break;
                        }
                    }
                    if (notDoneBlock != null) {
                        // We do the refined logic
                        int lastGuess = slowdownGuesses.get(notDoneBlock);

                        // replicate your approach to scale back the guess
                        if (lastGuess > 500) {
                            lastGuess -= 100;
                        } else if (lastGuess > 200) {
                            lastGuess -= 50;
                        } else if (lastGuess > 100) {
                            lastGuess -= 10;
                        } else if (lastGuess > 50) {
                            lastGuess -= 5;
                        } else if (lastGuess > 20) {
                            lastGuess -= 1;
                        }

                        // call Diviner
                        int slowdown = Diviner.DivineComplex(
                                RunID,
                                formattedMethodName,
                                notDoneBlock,
                                Benchmark,
                                iterations,
                                lowFootPrint,
                                compilerReplay,
                                slowdownAmount,
                                slowdownGuesses.get(notDoneBlock),
                                lastGuess);
                        // store final
                        storeFinalSlowdown(
                                formattedMethodName.replace("::", "."),
                                notDoneBlock,
                                slowdown);
                        trackers.get(notDoneBlock).isDone = true;
                    }

                    // We’re effectively done for this method
                    allBlocksCompleted = true;
                }

                // 3.3.7 If lowFootPrint is set, generate a report & remove VTune runs
                // (same as your existing logic)
                if (lowFootPrint) {
                    String directoryPath = "/home/hb478/repos/GTSlowdownSchedular/Data/" + RunID
                            + "_SlowDown_Data/LowFootPrintDumps";
                    File directory = new File(directoryPath);
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }
                    String fileName = new File(runLocation).getName();
                    String outputFilePath2 = directoryPath + "/" + fileName + "_" + method + ".txt";

                    // Generate VTune method-block report
                    VTuneAnalyzer.generateMethodBlockVTuneReport(fileName, method, outputFilePath2);

                    // Clean up
                    RemoveVtuneRun.run(runLocation);
                }
            } // end while !allBlocksCompleted

            // 3.4 Write final results for this method
            GTBuildFinalSlowdownFile.writeToFile("Final_" + formattedMethodName.replace("::", "."), RunID);
        } // end for each method
    }


    public static void runComplexJumpStart(
        String Benchmark,
        int iterations,
        String RunID,
        boolean lowFootPrint,
        boolean compilerReplay,
        double slowdownAmount) {
    // 1. Load marker-phase data
    MarkerPhaseDataLookup.loadData(RunID);

    // 2. Gather all methods
    List<String> methods = MarkerPhaseDataLookup.getAllMethods();

    Collections.reverse(methods);

    try {
        SlowdownFileRetriever.loadMethodBlockCostsFromJSON("/home/hb478/repos/GTSlowdownSchedular/FinalDataRefined100/"+Benchmark+"/Final_"+Benchmark+".json");
    } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }

    // 3. For each method, do the new "complex" logic
    for (String method : methods) {

        // Clean up final slowdown data for each iteration of this method
        GTBuildFinalSlowdownFile.slowdownData.clear();
        GTBuildFinalSlowdownFile.backendSlowdownData.clear();

        GTBuildSlowdownFile.slowdownData.clear();
        GTBuildSlowdownFile.backendSlowdownData.clear();

        // Format the method name as needed for usage elsewhere
        String formattedMethodName = method.replace(".", "::");

        // 3.1 Get all block infos for this method
        List<BlockInfo> blocks = MarkerPhaseDataLookup.getBenchmarkEntries(formattedMethodName);

        // 3.2 Create a tracker for each block, if it’s not trivially small
        // and start each with slowdown guess = 1
        Map<BlockInfo, BlockSlowdownTracker> trackers = new HashMap<>();
        Map<BlockInfo, Integer> slowdownGuesses = new HashMap<>();

        for (BlockInfo blockInfo : blocks) {
            if (blockInfo.baseCpuTime < 0.01) {
                // Skip extremely small / negligible blocks
                continue;
            }
            // Initialize with slowdown=1
            slowdownGuesses.put(blockInfo, 1);
            
            // as this is a jump start, we can start with a higher guess
            if (blockInfo.backendBlock) {
                slowdownGuesses.put(blockInfo, SlowdownFileRetriever.getBackendBlockCost(formattedMethodName.replace("::", "."), blockInfo.vtuneBlock));
            }
            else{
                slowdownGuesses.put(blockInfo, SlowdownFileRetriever.getBlockCost(formattedMethodName.replace("::", "."), blockInfo.vtuneBlock));
            }

            // Create a tracker using the new class
            BlockSlowdownTracker tracker = new BlockSlowdownTracker(blockInfo, slowdownAmount);
            // We also want to record an initial "measurement" of 0 time, just to set it up
            // or we can leave it until after the first actual measurement
            trackers.put(blockInfo, tracker);
        }

        if (trackers.isEmpty()) {
            // If no valid blocks, just skip
            continue;
        }

        // 3.3 We'll iterate until all blocks are done
        boolean allBlocksCompleted = false;
        int iterationCounter = 0;

        while (!allBlocksCompleted) {
            allBlocksCompleted = true; // assume done until proven otherwise
            iterationCounter++;

            for (Map.Entry<BlockInfo, BlockSlowdownTracker> entry : trackers.entrySet()) {
                BlockInfo block = entry.getKey();
                BlockSlowdownTracker tracker = entry.getValue();

                if (!tracker.isDone()) {
                    // Retrieve or set the slowdown guess
                    int currentGuess = slowdownGuesses.getOrDefault(block, 1);

                    // Write this entry to the slowdown file
                    GTBuildSlowdownFile.addEntry(
                            formattedMethodName,
                            block.graalID,
                            block.vtuneBlock,
                            currentGuess,
                            block.backendBlock);

                    // Mark that we are not done if at least one block is not done
                    allBlocksCompleted = false;
                }
            }

            // If everything was done before we even run, break the loop
            if (allBlocksCompleted) {
                break;
            }

            // 3.3.2 write out the slowdown file to disk
            String pathToSlowdownFile = GTBuildSlowdownFile.writeToFile(
                    "_Iter" + iterationCounter + "_" + formattedMethodName,
                    RunID);

            // 3.3.3 Run VTune with these slowdowns
            String uniqueRunID = RunID + "_Iter" + iterationCounter;
            String runLocation = VTuneRunner.runVtune(
                    Benchmark,
                    iterations,
                    AWFYBenchmarksLookUp.getExtraArgs(Benchmark),
                    false,
                    true,
                    pathToSlowdownFile,
                    uniqueRunID,
                    compilerReplay);

            // 3.3.4 Get the CPU times from VTune
            Map<Integer, Double> blocksSpeeds = VTuneAnalyzer.getCpuTimesForAllBlocks(runLocation,
                    formattedMethodName);

            // 3.3.5 For each block that is not done, record measurement & see if we
            // overshot
            int blocksDoneCount = 0;
            BlockInfo lastActiveBlock = null;

            for (Map.Entry<BlockInfo, BlockSlowdownTracker> entry : trackers.entrySet()) {
                BlockInfo block = entry.getKey();
                BlockSlowdownTracker tracker = entry.getValue();

                if (tracker.isDone()) {
                    // already finalized
                    blocksDoneCount++;
                    continue;
                }

                // retrieve current guess
                int currentGuess = slowdownGuesses.get(block);

                // read measured CPU time from the dictionary (or a default)
                double measuredTime = blocksSpeeds.getOrDefault(block.vtuneBlock, 9999.0);

                // record the measurement in the tracker
                // (this call sets 'last' to what 'current' was,
                // and 'current' to the new measurement)
                tracker.recordMeasurement(currentGuess, measuredTime);

                // see if we overshot enough to finalize
                Integer maybeFinal = tracker.determineBestSlowdownIfOvershot();
                if (maybeFinal != null) {
                    // If we just got a final slowdown, store it
                    storeFinalSlowdown(
                            formattedMethodName.replace("::", "."),
                            block,
                            maybeFinal);
                    blocksDoneCount++;
                } else {
                    // still under target => we may need to increment
                    if (tracker.isUnderTarget()) {
                        // increment slowdown
                        int newGuess = currentGuess + 1;

                        // We can also replicate your "faster increment" logic:
                        if (currentGuess > 500) {
                            newGuess += 100;
                        } else if (currentGuess > 200) {
                            newGuess += 50;
                        } else if (currentGuess > 100) {
                            newGuess += 10;
                        } else if (currentGuess > 50) {
                            newGuess += 5;
                        } else if (currentGuess > 20) {
                            newGuess += 1;
                        }

                        slowdownGuesses.put(block, newGuess);
                    } else {
                        // If it's neither under target nor overshot,
                        // it might already be in the sweet spot.
                        // This can happen if measuredTime is extremely close
                        // to targetTime or exactly equals it.
                        // Mark as final if you want, or keep iterating:
                        tracker.isDone = true;
                        storeFinalSlowdown(
                                formattedMethodName.replace("::", "."),
                                block,
                                currentGuess);
                        blocksDoneCount++;
                    }
                }

                lastActiveBlock = tracker.isDone() ? lastActiveBlock : block;
            }

            // 3.3.6 Check if we only have one block left, refine with Diviner
            // (like your original code)
            int totalBlocks = trackers.size();
            if (blocksDoneCount == totalBlocks - 1) {
                // Identify the last not-done block
                BlockInfo notDoneBlock = null;
                for (BlockInfo b : trackers.keySet()) {
                    if (!trackers.get(b).isDone()) {
                        notDoneBlock = b;
                        break;
                    }
                }
                if (notDoneBlock != null) {
                    // We do the refined logic
                    int lastGuess = slowdownGuesses.get(notDoneBlock);

                    // replicate your approach to scale back the guess
                    if (lastGuess > 500) {
                        lastGuess -= 100;
                    } else if (lastGuess > 200) {
                        lastGuess -= 50;
                    } else if (lastGuess > 100) {
                        lastGuess -= 10;
                    } else if (lastGuess > 50) {
                        lastGuess -= 5;
                    } else if (lastGuess > 20) {
                        lastGuess -= 1;
                    }

                    // call Diviner
                    int slowdown = Diviner.DivineComplex(
                            RunID,
                            formattedMethodName,
                            notDoneBlock,
                            Benchmark,
                            iterations,
                            lowFootPrint,
                            compilerReplay,
                            slowdownAmount,
                            slowdownGuesses.get(notDoneBlock),
                            lastGuess);
                    // store final
                    storeFinalSlowdown(
                            formattedMethodName.replace("::", "."),
                            notDoneBlock,
                            slowdown);
                    trackers.get(notDoneBlock).isDone = true;
                }

                // We’re effectively done for this method
                allBlocksCompleted = true;
            }

            // 3.3.7 If lowFootPrint is set, generate a report & remove VTune runs
            // (same as your existing logic)
            if (lowFootPrint) {
                String directoryPath = "/home/hb478/repos/GTSlowdownSchedular/Data/" + RunID
                        + "_SlowDown_Data/LowFootPrintDumps";
                File directory = new File(directoryPath);
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                String fileName = new File(runLocation).getName();
                String outputFilePath2 = directoryPath + "/" + fileName + "_" + method + ".txt";

                // Generate VTune method-block report
                VTuneAnalyzer.generateMethodBlockVTuneReport(fileName, method, outputFilePath2);

                // Clean up
                RemoveVtuneRun.run(runLocation);
            }
        } // end while !allBlocksCompleted

        // 3.4 Write final results for this method
        GTBuildFinalSlowdownFile.writeToFile("Final_" + formattedMethodName.replace("::", "."), RunID);
    } // end for each method
}

    /**
     * Helper method to store a final slowdown value for a block
     * in GTBuildFinalSlowdownFile's data structures.
     */
    private static void storeFinalSlowdown(
            String methodNameDot,
            BlockInfo block,
            int finalSlowdownValue) {
        String blockKey = block.graalID + " (Vtune Block " + block.vtuneBlock + ")";
        if (block.backendBlock) {
            GTBuildFinalSlowdownFile.backendSlowdownData
                    .computeIfAbsent(methodNameDot, k -> new HashMap<>())
                    .put(blockKey, finalSlowdownValue);
        } else {
            GTBuildFinalSlowdownFile.slowdownData
                    .computeIfAbsent(methodNameDot, k -> new HashMap<>())
                    .put(blockKey, finalSlowdownValue);
        }
    }

}
