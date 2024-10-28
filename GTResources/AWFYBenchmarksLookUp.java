package GTResources;

import org.json.JSONObject;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class AWFYBenchmarksLookUp {
        public static int getQueensExtraArgs() {
        try {
            String filePath = "AWFY_Benchmarks.json";
            // Read the JSON file content as a String
            String jsonString = new String(Files.readAllBytes(Paths.get(filePath)));

            // Parse the JSON string into a JSONObject
            JSONObject jsonObject = new JSONObject(jsonString);

            // Navigate to the "Queens" object and retrieve "extra_args"
            return jsonObject.getJSONObject("AWFY Benchmarks")
                    .getJSONObject("Queens")
                    .getInt("extra_args");

        } catch (IOException e) {
            e.printStackTrace();
            return -1; // Return a default/error value if file reading fails
        }

    }
}
