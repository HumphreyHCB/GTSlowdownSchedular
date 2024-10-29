package VTune;
import java.io.*;
import java.util.*;

public class VTuneAnalyzer {
    
    public static Map<String, Double> getAllMethodsFoundByVtune(String RunID) {
        Map<String, Double> methodPercentages = new LinkedHashMap<>();
        double totalCpuTime = 0.0;
        Map<String, Double> methodCpuTimes = new LinkedHashMap<>();
    
        // Construct the VTune command to get method level analysis
        String command = String.format(
                "vtune -report hotspots -r %s -group-by=function -column=function,\"CPU Time:Self\"",
                "/home/hb478/repos/GTSlowdownSchedular/Data/" + RunID
        );
    
        try {
            // Using ProcessBuilder to run the shell command and capture the output
            ProcessBuilder builder = new ProcessBuilder("/bin/sh", "-c", command);
            builder.redirectErrorStream(true);
    
            // Start the process
            Process process = builder.start();
    
            // Read the output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            boolean dataSection = false;
            while ((line = reader.readLine()) != null) {
                // Skip lines until the data section is found
                if (line.startsWith("Function") && line.contains("CPU Time")) {
                    dataSection = true;
                    continue;
                }
                if (!dataSection || line.trim().isEmpty() || line.startsWith("----") || line.startsWith("vtune:")) {
                    continue;
                }
    
                // Parse the actual data lines
                String[] tokens = line.trim().split("\\s+", 3);
                if (tokens.length >= 3) {
                    try {
                        String functionName = tokens[2];
                        double cpuTime = Double.parseDouble(tokens[1].replace("s", ""));
                        methodCpuTimes.put(functionName, cpuTime);
                        totalCpuTime += cpuTime;
                    } catch (NumberFormatException e) {
                        //System.out.println("Skipping invalid line: " + line);
                    }
                }
            }
    
            // Waiting for the process to complete
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Error occurred while retrieving VTune report.");
            }
    
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    
        // Calculate and add percentage of total runtime for each method
        if (totalCpuTime > 0) {
            for (Map.Entry<String, Double> entry : methodCpuTimes.entrySet()) {
                double percentage = (entry.getValue() / totalCpuTime) * 100;
                methodPercentages.put(entry.getKey().replaceAll("\\([^)]*\\)$", ""), percentage);
            }
        } else {
            System.out.println("No valid data found for CPU time calculation.");
        }
    
        return methodPercentages;
    }


    // Method to generate the VTune report and save it to a .txt file
    public static void generateMethodBlockVTuneReport(String vtunePath, String functionName, String outputFileName) {
        // Construct the VTune command with the dynamic path and function name
        String command = String.format(
                "vtune -report hotspots -r %s -source-object function=%s -group-by=basic-block,address -column=block,\"CPU Time:Self\",assembly",
                "Data/"+ vtunePath, functionName
        );

        try {
            // Create a file object with the specified .txt output file name
            File outputFile = new File(outputFileName);

            // Using ProcessBuilder to run the shell command and redirect the output to the file
            ProcessBuilder builder = new ProcessBuilder("/bin/sh", "-c", command);
            builder.redirectErrorStream(true);
            builder.redirectOutput(outputFile);

            // Start the process
            Process process = builder.start();

            // Waiting for the process to complete
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("VTune report generated successfully and saved to " + outputFileName);
            } else {
                System.out.println("Error occurred while generating the VTune report.");
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


        /**
     * Method to get the CPU time for a specific function and block ID.
     *
     * @param vtunePath    the path to the VTune run
     * @param functionName the function name, e.g., "Queens::placeQueen"
     * @param blockID      the block ID for which the CPU time is needed
     * @return the CPU time as a double, or -1 if not found
     */
    public static double getCpuTimeForBlock(String vtunePath, String functionName, int blockID) {
        // Convert "Queens::placeQueen" to "Queens.placeQueen"
        String formattedFunctionName = functionName.replace(".", "::");

        // Construct the VTune command to retrieve block information
        String command = String.format(
                "vtune -report hotspots -r %s -source-object function=%s -group-by=basic-block,address -column=block,\"CPU Time:Self\",assembly",
                vtunePath, formattedFunctionName
        );

        try {
            // Execute the command and capture output
            ProcessBuilder builder = new ProcessBuilder("/bin/sh", "-c", command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            // Parse the output to find the specified block and its CPU time
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                String targetBlock = "Block " + blockID;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(targetBlock)) {
                        // Extract CPU time
                        String cpuTimeStr = extractCpuTime(line);
                        if (cpuTimeStr != null) {
                            return Double.parseDouble(cpuTimeStr.replace("s", ""));
                        }
                    }
                }
            }

            // Wait for the process to complete
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return -1; // Return -1 if CPU time for the specified block is not found
    }

    // Helper method to extract CPU time from a line (if present)
    private static String extractCpuTime(String line) {
        // Regex to match CPU time in the format "0.329s" at the end of the line
        String cpuTimeRegex = ".*\\s(\\d+\\.\\d+)s$";
        if (line.matches(cpuTimeRegex)) {
            return line.replaceAll(cpuTimeRegex, "$1");
        }
        return null; // No CPU time found
    }

    // Main method to demonstrate the usage of the generateVTuneReport method
    public static void main(String[] args) {
        // Example usage
        // String vtunePath = "/home/hb478/intel/vtune/projects/QueensVTuneTests/r096hs";
        // String functionName = "Queens::placeQueen";

        // // Generating a report and saving it to a new .txt file
        // String outputFileName = "vtune_report_"+functionName+".txt";  // You can choose any name for this
        // generateMethodBlockVTuneReport(vtunePath, functionName, outputFileName);

        String vtunePath = "/home/hb478/repos/GTSlowdownSchedular/Data/2024_10_28_12_08_27";
        Map<String, Double> methods = getAllMethodsFoundByVtune(vtunePath);
        if (methods.isEmpty()) {
            System.out.println("No methods to display.");
        } else {
            methods.forEach((method, percentage) -> System.out.printf("%.2f%% %s%n", percentage, method));
        }
    }
}
