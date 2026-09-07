package Tests;

import GTResources.AWFYBenchmarksLookUp;
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
public class VTuneBenchmarkRun {

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

    public static void main(String[] args) {
        for (int i = 1; i < 11; i++) {

            String path = "";
            boolean lowFootPrint = true;
            int iterations = 500;
            path = "/home/hb478/repos/GTSlowdownSchedular/FinalDataRefined100";
            for (String benchmark : AWFY_BENCHMARKS) {
                run(path, lowFootPrint, benchmark, iterations, true, "100_" + i);
            }
        }

    }

    public static void run(String path, boolean lowFootPrint, String benchmark, int iterations,
            boolean CompilerReplay, String identifer) {

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


        RemoveVtuneRun.run(pathToNomralRun);
        RemoveVtuneRun.run(destinationPath);

    }

    public static String generateId() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
        return now.format(formatter);
    }
}
