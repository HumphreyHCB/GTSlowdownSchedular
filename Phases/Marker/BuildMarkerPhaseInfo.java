package Phases.Marker;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

/**
 * BuildMarkerPhaseInfo
 */
public class BuildMarkerPhaseInfo {

    public static void main(String[] args) {
        build("2024_10_29_14_48_26");
    }

    private static String readFileContents(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (FileReader reader = new FileReader(file)) {
            int ch;
            while ((ch = reader.read()) != -1) {
                content.append((char) ch);
            }
        }
        return content.toString();
    }

    public static boolean build(String markerRunId) {
        String normalRunPath = String.format("Data/%s_NormalRun/result.json", markerRunId);
        String markerRunPath = String.format("Data/%s_MarkerRun/result.json", markerRunId);

        File normalRunFile = new File(normalRunPath);
        File markerRunFile = new File(markerRunPath);

        if (!normalRunFile.exists() || !markerRunFile.exists()) {
            System.out.println("One or both result files are missing.");
            return false;
        }

        try {
            String normalRunContent = readFileContents(normalRunFile);
            String markerRunContent = readFileContents(markerRunFile);

            JSONObject normalRunJson = new JSONObject(normalRunContent);
            JSONObject markerRunJson = new JSONObject(markerRunContent);

            // Check that both have the same methods and blocks
            if (haveSameMethodsAndBlocks(normalRunJson, markerRunJson) > 1.0) {
                //System.out.println("Methods and blocks do not match between the runs.");
                return false;
            }

            // Create new JSON with BaseCpuTime from normalRunJson
            JSONObject updatedMarkerRunJson = new JSONObject(markerRunJson.toString());

            for (String method : normalRunJson.keySet()) {
                JSONArray normalBlocks = normalRunJson.getJSONArray(method);
                JSONArray markerBlocks = updatedMarkerRunJson.getJSONArray(method);

                for (int i = 0; i < markerBlocks.length(); i++) {
                    JSONObject markerBlock = markerBlocks.getJSONObject(i);
                    JSONObject normalBlock = normalBlocks.getJSONObject(i);

                    if (normalBlock.has("CpuTime")) {
                        markerBlock.put("BaseCpuTime", normalBlock.get("CpuTime"));
                        markerBlock.remove("CpuTime");
                        markerBlock.put("LineCount", normalBlock.get("LineCount"));
                    }
                }
            }

            // Write updated JSON to a new file
            String outputPath = String.format("Data/%s_MarkerRun/MarkerPhaseInfo.json", markerRunId);
            try (FileWriter writer = new FileWriter(outputPath)) {
                writer.write(updatedMarkerRunJson.toString(4)); // Pretty print with an indentation of 4
            }

            return true;

        } catch (IOException e) {
            System.err.println("Error processing JSON files: " + e.getMessage());
            return false;
        }
    }

    private static double haveSameMethodsAndBlocks(JSONObject normalRunJson, JSONObject markerRunJson) {
        boolean match = true;
        double totalMissingCpuTime = 0.0;

        if (!normalRunJson.keySet().equals(markerRunJson.keySet())) {
            match = false;
        }

        for (String method : normalRunJson.keySet()) {
            if (!markerRunJson.has(method)) {
                match = false;
                continue;
            }

            JSONArray normalBlocks = normalRunJson.getJSONArray(method);
            JSONArray markerBlocks = markerRunJson.getJSONArray(method);

            // if (normalBlocks.length() != markerBlocks.length()) {
            //     match = false;
            //     continue;
            // }

            for (int i = 0; i < normalBlocks.length(); i++) {
                String normalVtuneBlock = normalBlocks.getJSONObject(i).getString("VtuneBlock");
                boolean blockFound = false;

                for (int j = 0; j < markerBlocks.length(); j++) {
                    String markerVtuneBlock = markerBlocks.getJSONObject(j).getString("VtuneBlock");
                    if (normalVtuneBlock.equals(markerVtuneBlock)) {
                        blockFound = true;
                        break;
                    }
                }

                if (!blockFound) {
                    match = false;
                    if (normalBlocks.getJSONObject(i).has("CpuTime")) {
                        System.out.println("Cant Find " + normalBlocks.getJSONObject(i).getString("VtuneBlock") + " Cost: " + normalBlocks.getJSONObject(i).getDouble("CpuTime"));
                        totalMissingCpuTime += normalBlocks.getJSONObject(i).getDouble("CpuTime");
                    }
                }
            }
        }

        if (!match) {
            System.out.println("Methods and blocks do not match between the runs.");
            System.out.println("Total missing CPU time: " + totalMissingCpuTime + "s");
        }

        return totalMissingCpuTime;
    }
}
