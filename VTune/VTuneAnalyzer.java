package VTune;
import java.io.*;
import java.util.*;

public class VTuneAnalyzer {
    
    public static Map<String, Double> getAllMethodsFoundByVtune(String RunID) {
        //Map<String, Double> methodPercentages = new LinkedHashMap<>();
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
                //System.out.println(line);
                if (tokens.length >= 3) {
                    try {
                        String functionName = tokens[2];
                        double cpuTime = Double.parseDouble(tokens[1].replace("s", ""));
                        methodCpuTimes.put(functionName.replaceAll("\\([^)]*\\)$", ""), cpuTime + methodCpuTimes.getOrDefault(functionName.replaceAll("\\([^)]*\\)$", ""), 0.0)); // sometimes it may have multiple entries for the same function, e.g. recomp
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
        // if (totalCpuTime > 0) {
       //      for (Map.Entry<String, Double> entry : methodCpuTimes.entrySet()) {
        //         double percentage = (entry.getValue() / totalCpuTime) * 100;
        //         methodPercentages.put(entry.getKey().replaceAll("\\([^)]*\\)$", ""), percentage);
        //     }
        // } else {
        //     System.out.println("No valid data found for CPU time calculation.");
        // }
    
        return methodCpuTimes;
    }


    // Method to generate the VTune report and save it to a .txt file
    public static void generateMethodBlockVTuneReport(String vtunePath, String functionName, String outputFileName) {
        // Construct the VTune command with the dynamic path and function name
        functionName = functionName.replace("$$", "\\$\\$").replace("$", "\\$").replace(";", "\\;").replace("::", "\\:\\:");
        vtunePath = vtunePath.replace("$$", "\\$\\$").replace("$", "\\$");
        
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
                System.out.println("Tryed to output here: " + outputFileName);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

        /**
     * Method to get the total CPU time for all blocks with CPU time (excluding individual instructions).
     *
     * @param vtunePath    the path to the VTune run
     * @param functionName the function name, e.g., "Queens::placeQueen"
     * @return the total CPU time as a double, or -1 if not found
     */
    public static double getTotalCpuTimeForMethodsBlocks(String vtunePath, String functionName) {
        // Convert "Queens::placeQueen" to "Queens.placeQueen"
        
        if (functionName.contains("$")) {
            functionName = functionName.replace("$", "\\$");
        }

        if (vtunePath.contains("$")) {
            vtunePath = vtunePath.replace("$", "\\$");
        }
        
        
        String formattedFunctionName = functionName.replace(".", "::");


        // Construct the VTune command to retrieve block information
        String command = String.format(
                "vtune -report hotspots -r %s -source-object function=%s -group-by=basic-block,address -column=block,\"CPU Time:Self\",assembly",
                vtunePath, formattedFunctionName
        );

        double totalCpuTime = 0.0;

        try {
            // Execute the command and capture output
            ProcessBuilder builder = new ProcessBuilder("/bin/sh", "-c", command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            // Parse the output to accumulate CPU time for block lines only
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Check if the line represents a block with a CPU time (e.g., "Block 3 0.048s")
                    if (line.matches(".*(?<!<)Block\\s+\\d+(?!>).*")) {
                        String cpuTimeStr = extractCpuTime(line);
                        if (cpuTimeStr != null && !cpuTimeStr.equals("0s")) {  // Exclude blocks with 0 CPU time
                            totalCpuTime += Double.parseDouble(cpuTimeStr.replace("s", ""));
                        }
                    }
                }
            }

            // Wait for the process to complete
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return totalCpuTime > 0 ? totalCpuTime : -1; // Return total CPU time, or -1 if no blocks had CPU time
    }
    
    public static Map<Integer, Double> getCpuTimesForAllBlocks(String vtunePath, String functionName) {
        // Sanitize inputs to escape special characters
        if (functionName.contains("$")) {
            functionName = functionName.replace("$", "\\$");
        }
    
        if (vtunePath.contains("$")) {
            vtunePath = vtunePath.replace("$", "\\$");
        }
    
        String formattedFunctionName = functionName.replace(".", "::");
    
        // Construct the VTune command to retrieve block information
        String command = String.format(
                "vtune -report hotspots -r %s -source-object function=%s -group-by=basic-block,address -column=block,\"CPU Time:Self\",assembly",
                vtunePath, formattedFunctionName
        );
    
        // Map to store block IDs and their corresponding CPU times
        Map<Integer, Double> blockCpuTimes = new HashMap<>();
    
        try {
            // Execute the command and capture output
            ProcessBuilder builder = new ProcessBuilder("/bin/sh", "-c", command);
            builder.redirectErrorStream(true);
            Process process = builder.start();
    
            // Parse the output to find blocks and their CPU times
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                String blockPattern = ".*(?<!<)Block\\s+(\\d+)(?!>).*(\\d*\\.\\d+)s.*"; // Regex to extract Block ID and CPU time
    
                while ((line = reader.readLine()) != null) {
                    if (line.matches(blockPattern)) {
                        // Extract Block ID and CPU Time
                        String[] parts = line.split("\\s+");
                        Integer blockID = Integer.parseInt(parts[2].trim());
                        Double cpuTime = Double.parseDouble(parts[parts.length - 1].replace("s", "").trim());
    
                        blockCpuTimes.put(blockID, cpuTime);
                    }
                }
            }
    
            // Wait for the process to complete
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    
        return blockCpuTimes; // Return the map of Block IDs and CPU times
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
        if (functionName.contains("$")) {
            functionName = functionName.replace("$", "\\$");
        }

        if (vtunePath.contains("$")) {
            vtunePath = vtunePath.replace("$", "\\$");
        }

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
                String targetBlockPattern = String.format(".*(?<!<)Block\\s+%d(?!>).*\\d*\\.\\d+s.*", blockID);
                while ((line = reader.readLine()) != null) {
                    if (line.matches(targetBlockPattern)) {
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
        // whats wrong with this "Lc/CollisionDetector\$\$Lambda\:\:0x00007f37f4007820\;\:\:apply"
        String id = "2024_12_20_17_01_59_runE";
        String vtunePath = "/home/hb478/repos/GTSlowdownSchedular/Data/2024_12_20_17_01_59_runE";
       // generateMethodBlockVTuneReport(id,"deltablue::Planner::makePlan", vtunePath+"/runE1.txt");
        getCpuTimesForAllBlocks("/home/hb478/repos/GTSlowdownSchedular/Data/2025_01_07_22_40_12_Iter13", "Towers::moveDisks");
        //getAllMethodsFoundByVtune(id);
    }
}
