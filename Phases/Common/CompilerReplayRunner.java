package Phases.Common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import GTResources.AWFYBenchmarksLookUp;

public class CompilerReplayRunner {
    
    public static void run(String Benchmark, int iterations, String RunID) {
        generateReplay(Benchmark,iterations,RunID);
    }

    private static void generateReplay(String benchmark, int iterations, String RunID){
                Path folderPath = Paths.get("/home/hb478/repos/GTSlowdownSchedular/Data/"+RunID+"_CompilerReplay");
        if (Files.notExists(folderPath)) {
            try {
                Files.createDirectories(folderPath);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

                // Command to be built dynamically
        List<String> command = new ArrayList<>();
        
        // Base vtune command with fixed options
        command.add("vtune");
        command.add("-collect");
        command.add("hotspots");
        command.add("-r /home/hb478/repos/GTSlowdownSchedular/Data/" + RunID);
        //command.add("-r /mnt/vtune_ramdisk/" + RunID);
        command.add("-knob");
        command.add("sampling-mode=hw");
       // command.add("-quiet");
       command.add("-knob");
       if(AWFYBenchmarksLookUp.getIfSingleFile(benchmark)){
        command.add("enable-stack-collection=false");
       }
       else{
        command.add("enable-stack-collection=true");
       }
        //command.add("-knob");
       // command.add("stack-size=1024");
        //command.add("-mrte-mode managed");
        //command.add("--app-working-dir=/home/hb478/repos/are-we-fast-yet/benchmarks/Java/src");
        command.add("--");
        command.add("/home/hb478/repos/graal-instrumentation/vm/latest_graalvm_home/bin/java");

        //command.add("-Djdk.graal.TrivialInliningSize=0");

        // Add other fixed JVM options
        command.add("-Djdk.graal.IsolatedLoopHeaderAlignment=0");
        command.add("-Djdk.graal.LoopHeaderAlignment=0");

        command.add("-XX:+UseJVMCICompiler");
        command.add("-XX:+UseJVMCINativeLibrary");
        command.add("-XX:-TieredCompilation");
        command.add("-XX:-BackgroundCompilation");
        command.add("-Djdk.graal.DisableCodeEntryAlignment=true");

        if (benchmark.equals("Havlak")) {
            command.add("-Xms8g");
            command.add("-Xmx8g");
    
        }

        //command.add("-XX:CodeEntryAlignment=64");
        //command.add("-XX:OptoLoopAlignment=16");
        //command.add("-XX:InteriorEntryAlignment=16");

        command.add("-Djdk.graal.SaveProfiles=true");
        command.add("-Djdk.graal.OverrideProfiles=true");
        command.add("-Djdk.graal.SaveProfilesPath="+ folderPath);
        //command.add("-Djdk.graal.StrictProfiles=false");
        //command.add("-Djdk.graal.LoadProfiles=/home/hb478/repos/GTSlowdownSchedular/SaveProfiles");


        command.add("-cp");
        command.add("/home/hb478/repos/are-we-fast-yet/benchmarks/Java/benchmarks.jar");
        // Add benchmark and inner benchmark amount
        command.add("Harness");
        command.add(benchmark); // e.g. "Queens"
        command.add(iterations+ ""); // e.g. "Queens"
        command.add(String.valueOf(AWFYBenchmarksLookUp.getExtraArgs(benchmark))); // e.g. "5000"

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
           // return "/home/hb478/repos/GTSlowdownSchedular/Data/" + RunID;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            //return "";
        }



    }

}
