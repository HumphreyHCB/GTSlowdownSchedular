package Phases.Divining;

import java.util.List;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import GTResources.AWFYBenchmarksLookUp;
import Phases.Common.RemoveVtuneRun;
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
                if (blockInfo.baseCpuTime < 0.01) {
                    // blocks like these are too unstable, and might not aprea in all ones with no time
                }
                else{
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
                    GTBuildSlowdownFile.addEntry(formattedMethodName, block.graalID, block.vtuneBlock, currentSlowdown, block.backendBlock);
                }
            
                // Write the combined slowdown file for all blocks
                String pathToSlowdownFile = GTBuildSlowdownFile.writeToFile(
                    "_Iter" + iterationCounter + "_" + formattedMethodName,
                    RunID
                );
            
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
                        compilerReplay
                    );
            
                // Check results for each block

                Map<Integer, Double> blocksSpeeds = VTuneAnalyzer.getCpuTimesForAllBlocks(runLocation, formattedMethodName);
                int BlocksThatAreDone = 0;
                BlockInfo lastblock = null;
                for (Map.Entry<BlockInfo, Integer> entry : activeBlocks.entrySet()) {
                    
                    BlockInfo block = entry.getKey();
                    double currentBlockCPUSpeed = blocksSpeeds.getOrDefault(block.vtuneBlock, 10000.0); // Default to a high value if the block is not found
            
                    // Check if the block has reached its target speed
                    if (currentBlockCPUSpeed < block.baseCpuTime * slowdownAmount) {
                        entry.setValue(entry.getValue() + 1); // Increment slowdown for the block
                         if (entry.getValue() > 500) {
                            entry.setValue(entry.getValue() + 100); // Increment slowdown for the block again (faster Divining)
                         } else if (entry.getValue() > 200) {
                            entry.setValue(entry.getValue() + 50); // Increment slowdown for the block again (faster Divining)
                        } else if (entry.getValue() > 100) {
                            entry.setValue(entry.getValue() + 10); // Increment slowdown for the block again (faster Divining)
                        } else if (entry.getValue() > 50) {
                            entry.setValue(entry.getValue() + 5); // Increment slowdown for the block again (faster Divining)
                        } else if (entry.getValue() + 1 > 20) {
                            entry.setValue(entry.getValue() + 1); // Increment slowdown for the block again (faster Divining)
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
                    } else if ( activeBlocks.get(lastblock) > 200) {
                        lastGeuss = lastGeuss - 50; 
                   } else if ( activeBlocks.get(lastblock) > 100) {
                        lastGeuss = lastGeuss - 10; 
                   } else if ( activeBlocks.get(lastblock) > 50) {
                        lastGeuss = lastGeuss - 5; 
                   } else if ( activeBlocks.get(lastblock)  > 20) {
                        lastGeuss = lastGeuss - 1; 
                   }

                    int slowdown = Diviner.DivineComplex(RunID, formattedMethodName, lastblock, Benchmark, iterations, lowFootPrint, compilerReplay, slowdownAmount, activeBlocks.get(lastblock), lastGeuss);
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

}
