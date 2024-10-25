import java.io.File;
import java.io.IOException;

public class VTuneReporter {

    // Method to generate the VTune report and save it to a .txt file
    public static void generateVTuneReport(String vtunePath, String functionName, String outputFileName) {
        // Construct the VTune command with the dynamic path and function name
        String command = String.format(
                "vtune -report hotspots -r %s -source-object function=%s -group-by=basic-block,address -column=block,\"CPU Time:Self\",assembly",
                vtunePath, functionName
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

    // Main method to demonstrate the usage of the generateVTuneReport method
    public static void main(String[] args) {
        // Example usage
        String vtunePath = "/home/hb478/intel/vtune/projects/QueensVTuneTests/r096hs";
        String functionName = "Queens::placeQueen";

        // Generating a report and saving it to a new .txt file
        String outputFileName = "vtune_report.txt";  // You can choose any name for this
        generateVTuneReport(vtunePath, functionName, outputFileName);
    }
}
