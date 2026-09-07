package Tests;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import GTResources.AWFYBenchmarksLookUp;
import Phases.Common.RemoveVtuneRun;
import Phases.Divining.GTBuildSlowdownFile;
import Phases.Divining.MarkerPhaseDataLookup;
import Phases.Divining.MarkerPhaseDataLookup.BlockInfo;
import Phases.Marker.MarkerRunner;
import VTune.VTuneAnalyzer;
import VTune.VTuneRunner;

public class SelectiveBlockSlowdownTest {

    public static void main(String[] args) {
        boolean lowFootPrint = true;
        int iterations = 500;

        // Define the benchmarks, compUnits, graalIDs, and vtuneBlocks
        String[][] inputs = {
            // Richards
            // {"Richards", "richards.Scheduler.schedule", "21", "248"},
            // {"Richards", "richards.Scheduler.schedule", "19", "246"},
            // {"Richards", "richards.Scheduler.schedule", "35004", "280"},
        
            // Json
            // {"Json", "json.JsonPureStringParser.readStringInternal", "48", "56"},
            // {"Json", "json.JsonPureStringParser.isWhiteSpace", "5", "17"},
            // {"Json", "json.JsonPureStringParser.readArray", "153", "127"},
        
            // // Havlak
            // {"Havlak", "havlak.LoopTesterApp.main", "274", "431"},
            // {"Havlak", "havlak.HavlakLoopFinder.findLoops", "309", "823"},
            // {"Havlak", "havlak.HavlakLoopFinder.doDFS", "120", "145"},

            // List
            {"List", "List.tail", "27", "21"},
            //{"List", "List.tail", "24", "20"},
            //{"List", "List.tail", "21", "17"},
        };
        

        for (String[] input : inputs) {
            String benchmark = input[0];
            String compUnit = input[1];
            int graalID = Integer.parseInt(input[2]);
            int vtuneBlock = Integer.parseInt(input[3]);

            String path = "/home/hb478/repos/GTSlowdownSchedular/FinalDataRefined100";
            String ID = generateId();

            compilerReplayCopy(path, lowFootPrint, benchmark, iterations, ID);
            run(ID, path, lowFootPrint, benchmark, iterations, compUnit, graalID, vtuneBlock);
        }
    }

    public static void run(String ID, String path, boolean lowFootPrint, String benchmark, int iterations,
            String compUnit, int graalID, int vtuneBlock) {

                GTBuildSlowdownFile.slowdownData.clear();

        MarkerRunner.run(benchmark, iterations, ID, true);

        // 1. Load marker-phase data
        MarkerPhaseDataLookup.loadData(ID);

        // 2. Gather all methods
        List<String> methods = MarkerPhaseDataLookup.getAllMethods();

        // Format the method name as needed for usage elsewhere
        String formattedMethodName = compUnit.replace(".", "::");

        // 3.1 Get all block infos for this method
        List<BlockInfo> blocks = MarkerPhaseDataLookup.getBenchmarkEntries(formattedMethodName);

        // First we need to Generate the customSlowdown file
        String slowdownDataPath = "/home/hb478/repos/GTSlowdownSchedular/Data/" + ID + "_SlowDown_Data";
        try {
            Files.createDirectories(Paths.get(slowdownDataPath));
        } catch (Exception e) {
            e.printStackTrace();
        }

        double baseCpuTime = 0;
        for (BlockInfo block : blocks) {
            if (block.vtuneBlock == vtuneBlock) {
                System.out.println("Block: " + block.graalID + " " + block.vtuneBlock);
                System.out.println("Base Time: " + block.baseCpuTime);
                baseCpuTime = block.baseCpuTime;
            }
        }
        
        List<Double> slowdowns = new ArrayList<Double>();
        int slowdown = 0;
        double currentTime = 0.0;
        
        // Continue looping until currentTime is at least 5 greater than baseCpuTime
        while (currentTime < baseCpuTime + 5) {
        
            // Add a slowdown entry
            GTBuildSlowdownFile.addEntry(compUnit, graalID, vtuneBlock, slowdown, false);
        
            String pathToSlowdownFile = GTBuildSlowdownFile.writeToFile(
                    "_Iter_" + slowdown + "_" + graalID + "_" + vtuneBlock, ID);
        
            String runPath = VTuneRunner.runVtune(
                    benchmark,
                    iterations,
                    AWFYBenchmarksLookUp.getExtraArgs(benchmark),
                    false,
                    true,
                    pathToSlowdownFile,
                    ID + "_" + slowdown,
                    true
            );
        
            currentTime = VTuneAnalyzer.getCpuTimeForBlock(runPath, compUnit, vtuneBlock);
        
            if (lowFootPrint) {
                // Clean up the run
                RemoveVtuneRun.run(runPath);
            }
            System.out.println("Base Speed :" +  baseCpuTime);
            System.out.println("For Slowdown " + slowdown + " Time: " + currentTime);

            slowdown++;

            if (slowdown > 40) {
                slowdown++;
            }
        }
        


       // int count = 0;
        //for (Double double1 : slowdowns) {
            double percentageIncrease = ((currentTime - baseCpuTime) / baseCpuTime) * 100;
            System.out.println("For Slowdown " + slowdown + " Time: " + currentTime + " Percentage Increase: " + percentageIncrease + "%");

            // Write the result to a file
            String result = "For Slowdown " + slowdown + " Time: " + currentTime + " Percentage Increase: " + percentageIncrease + "%\n";
            Path resultFilePath = Paths.get(slowdownDataPath, "Results.txt");
            try {
                Files.write(resultFilePath, result.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //count++;
        //}

    }


    public static void runWithStart(String ID, String path, boolean lowFootPrint, String benchmark, int iterations,
            String compUnit, int graalID, int vtuneBlock, int start) {

                GTBuildSlowdownFile.slowdownData.clear();

       // MarkerRunner.run(benchmark, iterations, ID, true);

        // 1. Load marker-phase data
        MarkerPhaseDataLookup.loadData(ID);

        // 2. Gather all methods
        List<String> methods = MarkerPhaseDataLookup.getAllMethods();

        // Format the method name as needed for usage elsewhere
        String formattedMethodName = compUnit.replace(".", "::");

        // 3.1 Get all block infos for this method
        List<BlockInfo> blocks = MarkerPhaseDataLookup.getBenchmarkEntries(formattedMethodName);

        // First we need to Generate the customSlowdown file
        String slowdownDataPath = "/home/hb478/repos/GTSlowdownSchedular/Data/" + ID + "_SlowDown_Data";
        try {
            Files.createDirectories(Paths.get(slowdownDataPath));
        } catch (Exception e) {
            e.printStackTrace();
        }

        double baseCpuTime = 0;
        for (BlockInfo block : blocks) {
            if (block.vtuneBlock == vtuneBlock) {
                System.out.println("Block: " + block.graalID + " " + block.vtuneBlock);
                System.out.println("Base Time: " + block.baseCpuTime);
                baseCpuTime = block.baseCpuTime;
            }
        }
        
        List<Double> slowdowns = new ArrayList<Double>();
        int slowdown = start;
        double currentTime = 0.0;
        
        // Continue looping until currentTime is at least 5 greater than baseCpuTime
        while (currentTime < baseCpuTime + 5) {
        
            // Add a slowdown entry
            GTBuildSlowdownFile.addEntry(compUnit, graalID, vtuneBlock, slowdown, false);
        
            String pathToSlowdownFile = GTBuildSlowdownFile.writeToFile(
                    "_Iter_" + slowdown + "_" + graalID + "_" + vtuneBlock, ID);
        
            String runPath = VTuneRunner.runVtune(
                    benchmark,
                    iterations,
                    AWFYBenchmarksLookUp.getExtraArgs(benchmark),
                    false,
                    true,
                    pathToSlowdownFile,
                    ID + "_" + slowdown,
                    true
            );
        
            currentTime = VTuneAnalyzer.getCpuTimeForBlock(runPath, compUnit, vtuneBlock);
        
            if (lowFootPrint) {
                // Clean up the run
                RemoveVtuneRun.run(runPath);
            }
            System.out.println("Base Speed :" +  baseCpuTime);
            System.out.println("For Slowdown " + slowdown + " Time: " + currentTime);

            slowdown++;

            if (slowdown > 40) {
                slowdown++;
            }
        }
        


       // int count = 0;
        //for (Double double1 : slowdowns) {
            double percentageIncrease = ((currentTime - baseCpuTime) / baseCpuTime) * 100;
            System.out.println("For Slowdown " + slowdown + " Time: " + currentTime + " Percentage Increase: " + percentageIncrease + "%");

            // Write the result to a file
            String result = "For Slowdown " + slowdown + " Time: " + currentTime + " Percentage Increase: " + percentageIncrease + "%\n";
            Path resultFilePath = Paths.get(slowdownDataPath, "Results.txt");
            try {
                Files.write(resultFilePath, result.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //count++;
        //}

    }

    public static void compilerReplayCopy(String path, boolean lowFootPrint, String benchmark, int iterations,
            String ID) {

        String sourcePath = path + "/" + benchmark + "/" + benchmark + "_CompilerReplay";
        String destinationPath = "/home/hb478/repos/GTSlowdownSchedular/Data/" + ID + "_CompilerReplay";
        try {
            // Ensure destination directory exists
            Files.createDirectories(Paths.get(destinationPath));

            // Get all files in the source directory
            DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(sourcePath));
            for (Path file : stream) {
                if (Files.isRegularFile(file)) { // Only process files
                    Path destinationFile = Paths.get(destinationPath).resolve(file.getFileName());
                    Files.copy(file, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String generateId() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
        return now.format(formatter);
    }

}
