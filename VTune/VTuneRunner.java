package VTune;
import GTResources.AWFYBenchmarksLookUp;
import Phases.GTSchedular;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class VTuneRunner {

    public static String runVtune(String benchmark,int iterations, int innerBenchmarkAmount, 
                                boolean jdkGraalGTMarkBasicBlocks, boolean jdkGraalLIRGTSlowDown,
                                String lirBlockSlowdownFileName, String RunID, boolean compilerReplay) {
        // Command to be built dynamically
        List<String> command = new ArrayList<>();
        
        // Base vtune command with fixed options
        command.add("vtune");
        command.add("-collect");
        command.add("hotspots");
        command.add("-r /home/hb478/repos/GTSlowdownSchedular/Data/" + RunID);

        command.add("-knob");
        command.add("sampling-mode=hw");
        command.add("-quiet");
       command.add("-knob");
       if(AWFYBenchmarksLookUp.getIfSingleFile(benchmark)){
        command.add("enable-stack-collection=false");
       }
       else{
        command.add("enable-stack-collection=true");
       }



       command.add("--app-working-dir=/home/hb478/repos/are-we-fast-yet/benchmarks/Java/src");

        command.add("--");
        command.add("/home/hb478/repos/graal-instrumentation/vm/latest_graalvm_home/bin/java");

        // Add customizable JVM options, defaulting to false                        
        command.add("-Djdk.graal.LIRGTSlowDown=" + (jdkGraalLIRGTSlowDown ? "true" : "false"));
        command.add("-Djdk.graal.GTMarkBasicBlocks=" + (jdkGraalGTMarkBasicBlocks ? "true" : "false"));
        if (!lirBlockSlowdownFileName.equals("")) {
            command.add("-Djdk.graal.LIRBlockSlowdownFileName=" + lirBlockSlowdownFileName);
        }

        if (benchmark.equals("Havlak") || benchmark.equals("DeltaBlue")) {
            command.add("-Xms8g");
            command.add("-Xmx8g");
    
        }
        
        // Add other fixed JVM options
        command.add("-Djdk.graal.IsolatedLoopHeaderAlignment=0");
        command.add("-Djdk.graal.LoopHeaderAlignment=0");
        command.add("-Djdk.graal.DisableCodeEntryAlignment=true");

        command.add("-XX:+UseJVMCICompiler");
        command.add("-XX:+UseJVMCINativeLibrary");
        command.add("-XX:-TieredCompilation");
        command.add("-XX:-BackgroundCompilation");

        //command.add("-Djdk.graal.MixGTSlowdown=false");

        if (compilerReplay) {
            command.add("-Djdk.graal.StrictProfiles=false");
            command.add("-Djdk.graal.LoadProfiles=/home/hb478/repos/GTSlowdownSchedular/Data/"+ RunID.substring(0, 19) + "_CompilerReplay");
        }



        command.add("-cp");
        command.add("/home/hb478/repos/are-we-fast-yet/benchmarks/Java/benchmarks.jar");
        // Add benchmark and inner benchmark amount
        command.add("Harness");
        command.add(benchmark); // e.g. "Queens"
        command.add(iterations+ ""); // e.g. "Queens"
        command.add(String.valueOf(innerBenchmarkAmount)); // e.g. "5000"

        //System.out.println(command.toString());
        // Run the command using ProcessBuilder
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        try {
            // Start the process
            Process process = processBuilder.start();

            // Capture and print the output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Wait for the process to complete and check exit status
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("VTune command executed successfully.");
            } else {
                System.out.println("VTune command failed with exit code: " + exitCode);
            }
            return "/home/hb478/repos/GTSlowdownSchedular/Data/" + RunID;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void main(String[] args) {
        // Example arguments to customize the run
        String benchmark = "DeltaBlue"; // Benchmark name
        int innerBenchmarkAmount = AWFYBenchmarksLookUp.getExtraArgs(benchmark); // Inner benchmark amount
        boolean jdkGraalGTMarkBasicBlocks = false; // Dynamic JVM option, default to false
        boolean jdkGraalLIRGTSlowDown = true; // Dynamic JVM option, default to false
        String lirBlockSlowdownFileName = "/home/hb478/repos/GTSlowdownSchedular/Data/2024_12_20_17_01_59_SlowDown_Data/_deltablue::Planner::makePlan_290_14731.json"; // File path
        //String trivialInliningSize = "0"; // Dynamic JVM option
        String runId = "2024_12_20_17_01_59_runE";
        long start = System.currentTimeMillis();
        // Call runVtune method with customizable options
        runVtune(benchmark,500, innerBenchmarkAmount, jdkGraalGTMarkBasicBlocks, jdkGraalLIRGTSlowDown, lirBlockSlowdownFileName ,runId, true);

        long end = System.currentTimeMillis();

        // Calculate and print the elapsed time in seconds
        double elapsedTimeInSeconds = (end - start) / 1000.0;
        System.out.println("Elapsed time: " + elapsedTimeInSeconds + " seconds");
    }
}
