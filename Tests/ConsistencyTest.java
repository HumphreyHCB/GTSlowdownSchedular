package Tests;

import Phases.Common.CompilerReplayRunner;
import Phases.Marker.MarkerRunner;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConsistencyTest {

    private static final String[] AWFY_BENCHMARKS = {
        "DeltaBlue",
        "Richards",
        "Json",
        "CD",
        "Havlak",
        "Bounce",
        "List",
        "Mandelbrot",
        "NBody",
        "Permute",
        "Queens",
        "Sieve",
        "Storage",
        "Towers"
    };
    public static void testAWFYSuite() {
        int iterations = 100;  // or another value you prefer
        boolean lowFootPrint = true;

        for (String benchmark : AWFY_BENCHMARKS) {
            String ID = generateId();

            // Run compiler replay phase
            CompilerReplayRunner.run(benchmark, iterations, ID);

            // Run marker phase
            MarkerRunner.run(benchmark, iterations, ID, true);

            // Assuming a similar naming convention for log files as in the main method
            String logFilePath = "Tests/TestResults/" + ID + "_" + benchmark + ".txt";

            // Run comparison/check phase if required for each benchmark
            VTuneFileComparatorRunner.CheckCD(ID, logFilePath);

            // You can add logging or print statements here if needed:
            System.out.println("Completed benchmark: " + benchmark + " with ID: " + ID);
        }
    }

    public static void main(String[] args) {

        testAWFYSuite();
    //     int iterations = 100;
    //     boolean lowFootPrint = true;
    //     String benchmark = "Json";
    //     String ID = generateId();
    //     //String ID = "2024_12_20_14_50_53";

    //     CompilerReplayRunner.run(benchmark, iterations, ID);
    //     MarkerRunner.run(benchmark, iterations, ID, true);
    //    String logFilePath = "Tests/TestResults/"+ID+"_Json.txt"; // or any path you like
    //    VTuneFileComparatorRunner.CheckCD(ID, logFilePath);
        //MarkerRunner.run(benchmark, iterations, generateId());
        //MarkerRunner.run(benchmark, iterations, generateId());
        // ID = "2024_10_29_18_19_36";

    

    }

    public static String generateId() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
        return now.format(formatter);
    }
    
}