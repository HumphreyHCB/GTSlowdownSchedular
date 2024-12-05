package Phases.Marker;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
    
            // New JSON structure based only on normalRunJson
            JSONObject updatedMarkerRunJson = new JSONObject();
    
            for (String method : normalRunJson.keySet()) {
                JSONArray normalBlocks = normalRunJson.getJSONArray(method);
                JSONArray markerBlocks = markerRunJson.optJSONArray(method);
                JSONArray updatedBlocks = new JSONArray();
    
                if (markerBlocks == null) {
                    System.out.println("No matching method found in marker JSON for: " + method);
                    continue;
                }
    
                for (int i = 0; i < normalBlocks.length(); i++) {
                    JSONObject normalBlock = normalBlocks.getJSONObject(i);
                    String vtuneBlockId = normalBlock.optString("VtuneBlock");
    
                    // Look for a corresponding block in the marker run with the same VtuneBlock ID
                    JSONObject markerBlock = null;
                    for (int j = 0; j < markerBlocks.length(); j++) {
                        JSONObject tempMarkerBlock = markerBlocks.getJSONObject(j);
                        if (vtuneBlockId.equals(tempMarkerBlock.optString("VtuneBlock"))) {
                            markerBlock = tempMarkerBlock;
                            break;
                        }
                    }
                    if (markerBlock != null && normalBlock.has("CpuTime")) {
                        // Construct updated block based on normal run values
                        JSONObject updatedBlock = new JSONObject();
                        updatedBlock.put("VtuneBlock", vtuneBlockId);
                        updatedBlock.put("BaseCpuTime", normalBlock.get("CpuTime"));
                        //updatedBlock.put("LineCount", normalBlock.get("LineCount"));

                        // Carry over other fields from markerBlock if it exists
                        if (markerBlock != null) {
                            for (String key : markerBlock.keySet()) {
                                if (!key.equals("CpuTime")) { // Exclude CpuTime from marker run
                                    updatedBlock.put(key, markerBlock.get(key));
                                }
                            }
                        }
                        if (Double.parseDouble(normalBlock.get("CpuTime").toString()) < 0.09) {
                            // skip
                        }
                        else{
                        updatedBlocks.put(updatedBlock);
                        }
                    } else {
                        System.out
                                .println("Incomplete block data for VtuneBlock ID: " + vtuneBlockId + ". Skipping...");
                    }
                }
    
                // Add updated blocks to the new JSON under the method name
                updatedMarkerRunJson.put(method, updatedBlocks);
            }
    
            // Write updated JSON to a new file
            String outputPath = String.format("Data/%s_MarkerRun/MarkerPhaseInfo.json", markerRunId);
            try (FileWriter writer = new FileWriter(outputPath)) {
                writer.write(updatedMarkerRunJson.toString(4));
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
