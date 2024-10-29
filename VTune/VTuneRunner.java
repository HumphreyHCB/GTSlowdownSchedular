package VTune;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VTuneRunner {

    public static void runVtune(String benchmark,int iterations, int innerBenchmarkAmount, 
                                boolean jdkGraalGTMarkBasicBlocks, boolean jdkGraalLIRGTSlowDown,
                                String lirBlockSlowdownFileName, String RunID) {
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
        command.add("enable-stack-collection=true");
        command.add("-knob");
        command.add("stack-size=4096");
        command.add("--app-working-dir=/home/hb478/repos/are-we-fast-yet/benchmarks/Java/src");
        command.add("--");
        command.add("/home/hb478/repos/graal-instrumentation/vm/latest_graalvm_home/bin/java");

        // Add customizable JVM options, defaulting to false
        command.add("-Djdk.graal.LIRGTSlowDown=" + (jdkGraalLIRGTSlowDown ? "true" : "false"));
        command.add("-Djdk.graal.GTMarkBasicBlocks=" + (jdkGraalGTMarkBasicBlocks ? "true" : "false"));
        if (!lirBlockSlowdownFileName.equals("")) {
            command.add("-Djdk.graal.LIRBlockSlowdownFileName=" + lirBlockSlowdownFileName);
        }

        command.add("-Djdk.graal.TrivialInliningSize=0");

        // Add other fixed JVM options
        command.add("-XX:+UseJVMCICompiler");
        command.add("-XX:+UseJVMCINativeLibrary");
        command.add("-XX:-TieredCompilation");
        command.add("-XX:CompileCommand=dontinline,*::*");
        command.add("-XX:-BackgroundCompilation");

        // Add benchmark and inner benchmark amount
        command.add("Harness");
        command.add(benchmark); // e.g. "Queens"
        command.add(iterations+ ""); // e.g. "Queens"
        command.add(String.valueOf(innerBenchmarkAmount)); // e.g. "5000"

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

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Example arguments to customize the run
        String benchmark = "Queens"; // Benchmark name
        int innerBenchmarkAmount = 5000; // Inner benchmark amount
        boolean jdkGraalGTMarkBasicBlocks = false; // Dynamic JVM option, default to false
        boolean jdkGraalLIRGTSlowDown = false; // Dynamic JVM option, default to false
        String lirBlockSlowdownFileName = "/home/hb478/repos/graal-instrumentation/vm/BlockSlowdown1.json"; // File path
        String trivialInliningSize = "0"; // Dynamic JVM option

        // Call runVtune method with customizable options
        //runVtune(benchmark, innerBenchmarkAmount, jdkGraalGTMarkBasicBlocks, jdkGraalLIRGTSlowDown, lirBlockSlowdownFileName, trivialInliningSize);
    }
}
