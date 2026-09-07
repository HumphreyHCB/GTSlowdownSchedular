package Tests;

import GTResources.AWFYBenchmarksLookUp;
import Phases.GTSchedular;
import Phases.Common.RemoveVtuneRun;
import Phases.Marker.MethodTargeter;
import VTune.VTuneAnalyzer;
import VTune.VTuneRunner;
import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

// this tests is to find what the total sum of new slowdown is for each benchmark with a completed Slowdown File
public class SlowdownTest {

    private static final String[] AWFY_BENCHMARKS = {
            "Towers",
            "Richards",
            "List",
            "Json",
            "DeltaBlue",
            "CD",
            "Bounce",
            "Mandelbrot",
            "NBody",
            "Permute",
            "Queens",
            "Sieve",
            "Storage",
            "Havlak"
    };

    // we need to do a nomral run,
    // then we need to do a slowdown run
    // and then find every block and find its new slowodwon value and add it to the
    // total sum of be fore and now added time
    // all that needs to be set is the folder that contains all of the slowodwn
    // files
    public static void main(String[] args) {
        // Defaults (preserve current behaviour if args are missing)
        String path = "/home/hb478/repos/GTSlowdownSchedular/FinalBuboTests/WithoutProbe/AWFY";
        boolean lowFootPrint = false;

        String benchmark = "Mandelbrot";
        int iterations = 500;

        boolean compilerReplay = true;
        String identifer = "AUTO_PIPELINE";

        // Args we accept (ONLY these three):
        // 0: path (folder that contains Final_<Benchmark>.json)
        // 1: benchmark
        // 2: iterations
        // 3: EnableBubo
        if (args.length >= 1 && args[0] != null && !args[0].isBlank()) {
            path = args[0].trim();
        }
        if (args.length >= 2 && args[1] != null && !args[1].isBlank()) {
            benchmark = args[1].trim();
        }
        if (args.length >= 3 && args[2] != null && !args[2].isBlank()) {
            try {
                iterations = Integer.parseInt(args[2].trim());
            } catch (NumberFormatException e) {
                System.err.println("[ERROR] iterations must be an int, got: " + args[2]);
                System.err.println("Usage: SlowdownTest <path> <benchmark> <iterations>");
                throw e;
            }
        }

        String enableBubo = null;

        if (args.length >= 4 && args[3] != null && !args[3].isBlank()) {
            enableBubo = args[3].trim();

            enableBubo.toLowerCase();
            if (enableBubo.equals("true")) {
                GTSchedular.EnableBuboLIRPhase = true;
            } else if (enableBubo.equals("false")) {
                GTSchedular.EnableBuboLIRPhase = false;
            } else {
                System.err.println("[ERROR] EnableBubo must be either 'true' or 'false', got: " + enableBubo);
                System.err.println("Usage: SlowdownTest <path> <benchmark> <iterations> <EnableBubo>");
                throw new IllegalArgumentException("EnableBubo must be either 'true' or 'false', got: " + enableBubo);
            }
        }


        System.out.println("[INFO] SlowdownTest config:");
        System.out.println("  path       = " + path);
        System.out.println("  benchmark  = " + benchmark);
        System.out.println("  iterations = " + iterations);
        System.out.println("  EnableBubo     = " + enableBubo);
        System.out.println("  lowFootPrint   = " + lowFootPrint);
        System.out.println("  compilerReplay = " + compilerReplay);
        System.out.println("  identifer      = " + identifer);

        run(path, lowFootPrint, benchmark, iterations, compilerReplay, identifer);
    }


    public static void run(String path, boolean lowFootPrint, String benchmark, int iterations,
            boolean CompilerReplay, String identifer) {

        // boolean lowFootPrint = true;
        // int iterations = 500;

        String ID = generateId();
        String destinationPath = "";
        if (CompilerReplay) {

            String sourcePath = path + "/" + benchmark + "/" + benchmark + "_CompilerReplay";
            destinationPath = "/home/hb478/repos/GTSlowdownSchedular/Data/" + ID + "_CompilerReplay";
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

        // Now we need to do a normal Run
        String normalRunID = ID + "_NormalRun";
        String pathToNomralRun = VTuneRunner.runVtune(benchmark, iterations,
                AWFYBenchmarksLookUp.getExtraArgs(benchmark), false, false, "",
                normalRunID, CompilerReplay);

        // Now we need to do a slowdown Run
         String SlowdownFilePath = path + "/" + benchmark + "/Final_" + benchmark + ".json";
         String slowdownRunID = ID + "_slowdown";
         String pathToslowdownRun = VTuneRunner.runVtune(benchmark, iterations,
                 AWFYBenchmarksLookUp.getExtraArgs(benchmark), false, true, SlowdownFilePath,
                 slowdownRunID, CompilerReplay);

        // Find all methods that Vtune Identifes
        Map<String, Double> normalRunfoundMethods = VTuneAnalyzer.getAllMethodsFoundByVtune(normalRunID);

        // get only the methods that are of significants
        List<String> normalRunsignificantMethods = MethodTargeter.findSignificantMethod(normalRunfoundMethods);

        for (String method : normalRunsignificantMethods) {
            // Create output file paths with sanitized method names
             String outputFilePath1 = String.format(
                     "/home/hb478/repos/GTSlowdownSchedular/Data/%s/%s.txt",
                     slowdownRunID,
                     method.replaceAll("[\\/:*?\"<>|]", "_"));
            String outputFilePath2 = String.format(
                    "/home/hb478/repos/GTSlowdownSchedular/Data/%s/%s_normal.txt",
                    normalRunID,
                    method.replaceAll("[\\/:*?\"<>|]", "_"));

             //Generate VTune reports for both runs
             VTuneAnalyzer.generateMethodBlockVTuneReport(new File(pathToslowdownRun).getName(), method,
                     outputFilePath1);
            VTuneAnalyzer.generateMethodBlockVTuneReport(new File(pathToNomralRun).getName(), method, outputFilePath2);
        }

        // Assuming a similar naming convention for log files as in the main method
        String logFilePath = "Tests/TestResults/" + ID + "_" + benchmark + "_SlowdownTest_" + identifer + ".txt";

        CompareTwoRuns.compare(pathToNomralRun, pathToslowdownRun, normalRunsignificantMethods, logFilePath);
        if (lowFootPrint) {
            
        RemoveVtuneRun.run(pathToNomralRun);
        RemoveVtuneRun.run(pathToslowdownRun);
        RemoveVtuneRun.run(destinationPath);
        }

    }

    public static String generateId() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
        return now.format(formatter);
    }
}
