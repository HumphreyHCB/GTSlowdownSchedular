package Tests;

import GTResources.AWFYBenchmarksLookUp;
import Phases.Common.RemoveVtuneRun;
import Phases.Divining.GTBuildSlowdownFile;
import VTune.VTuneAnalyzer;
import VTune.VTuneRunner;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestASlowdown {
    
    public static void main(String[] args) {


        int iterations = 500;

        int slowdown = 75;
        boolean lowFootPrint = false;
        // 29 (Vtune Block 288)
        // Define the benchmarks, compUnits, graalIDs, and vtuneBlocks
        String[][] inputs = {
            {"Richards", "richards.Scheduler.schedule", "29", "288"}
        };

        String benchmark = inputs[0][0];
        String compUnit = inputs[0][1];
        int graalID = Integer.parseInt(inputs[0][2]);
        int vtuneBlock = Integer.parseInt(inputs[0][3]);

        String path = "/home/hb478/repos/GTSlowdownSchedular/FinalDataRefined100";
        String ID = generateId();

        compilerReplayCopy(path, lowFootPrint, benchmark, iterations, ID);

        
               // Format the method name as needed for usage elsewhere
               String formattedMethodName = compUnit.replace(".", "::");

                // First we need to Generate the customSlowdown file
                String slowdownDataPath = "/home/hb478/repos/GTSlowdownSchedular/Data/" + ID + "_SlowDown_Data";
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
        
            double currentTime = VTuneAnalyzer.getCpuTimeForBlock(runPath, compUnit, vtuneBlock);
        
            if (lowFootPrint) {
                // Clean up the run
                RemoveVtuneRun.run(runPath);
            }

            System.out.println("For Slowdown " + slowdown + " Time: " + currentTime);



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
