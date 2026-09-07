package Tests;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class VTuneFindUnmarkedBlocks {

    private static PrintWriter logWriter;

    public static void setLogWriter(PrintWriter w) {
        logWriter = w;
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

    public static boolean find(String markerRunId) {
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
            
            double comp_total_time = 0;

            for (String method : normalRunJson.keySet()) {
                JSONArray normalBlocks = normalRunJson.getJSONArray(method);
                JSONArray markerBlocks = markerRunJson.optJSONArray(method);
                JSONArray updatedBlocks = new JSONArray();
    
                if (markerBlocks == null) {
                    logWriter.println("No matching method found in marker JSON for: " + method);
                    continue;
                }
                double method_total_time = 0;
    
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
                        updatedBlocks.put(updatedBlock);
                    } else {
                        logWriter.println("Incomplete block data for VtuneBlock ID: " + vtuneBlockId + " Time : "+ normalBlock.get("CpuTime") +" . Skipping...");
                        method_total_time += normalBlock.getDouble("CpuTime");
                    }
                }
                logWriter.println("Total time missing for method: " + method + " is " + method_total_time);
                comp_total_time += method_total_time;
                // Add updated blocks to the new JSON under the method name
                updatedMarkerRunJson.put(method, updatedBlocks);
            }
            
            logWriter.println("Total time missing for all methods is : " + comp_total_time);
    
            return true;
    
        } catch (IOException e) {
            System.err.println("Error processing JSON files: " + e.getMessage());
            return false;
        }
    }
}
