package GTResources;

import org.json.JSONObject;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class AWFYBenchmarksLookUp {
    // Method to retrieve "extra_args" based on the benchmark name
    public static int getExtraArgs(String benchmarkName) {
        try {
            String filePath = "/home/hb478/repos/GTSlowdownSchedular/GTResources/AWFY_Benchmarks.json";
            // Read the JSON file content as a String
            String jsonString = new String(Files.readAllBytes(Paths.get(filePath)));

            // Parse the JSON string into a JSONObject
            JSONObject jsonObject = new JSONObject(jsonString);

            // Retrieve "extra_args" for the specified benchmark name
            if (jsonObject.getJSONObject("AWFY Benchmarks").has(benchmarkName)) {
                return jsonObject.getJSONObject("AWFY Benchmarks")
                        .getJSONObject(benchmarkName)
                        .getInt("extra_args");
            } else {
                System.out.println("Benchmark not found: " + benchmarkName);
                return -1; // Return a default/error value if the benchmark is not found
            }

        } catch (IOException e) {
            e.printStackTrace();
            return -1; // Return a default/error value if file reading fails
        }
    }
}
