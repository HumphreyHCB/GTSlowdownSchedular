package Phases.Marker;

import VTune.VTuneAnalyzer;
import VTune.VTuneReportRipper;
import VTune.VTuneReportRipper.BlockData;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class BuildSlowdownTargetsList {

    public static void run(String markerRunId, List<String> significantMethods) {
        if (significantMethods == null || significantMethods.isEmpty()) {
            System.out.println("No significant methods provided.");
            return;
        }

        for (String method : significantMethods) {
            String outputFilePath = String.format("/home/hb478/repos/GTSlowdownSchedular/Data/%s/%s.txt", markerRunId,
                    method.replaceAll("[\\/:*?\"<>|]", "_"));
            VTuneAnalyzer.generateMethodBlockVTuneReport(markerRunId, method, outputFilePath);
        }

        processGeneratedFiles(markerRunId, significantMethods);
    }

    private static void processGeneratedFiles(String markerRunId, List<String> significantMethods) {
        String directoryPath = String.format("/home/hb478/repos/GTSlowdownSchedular/Data/%s/", markerRunId);
        VTuneReportRipper ripper = new VTuneReportRipper();
        JSONObject resultJson = new JSONObject();

        for (String method : significantMethods) {
            String filePath = directoryPath + method.replaceAll("[\\/:*?\"<>|]", "_") + ".txt";
            File file = new File(filePath);
            if (file.exists()) {
                Map<String, BlockData> blocks = ripper.processFileIntoBlocks(filePath);
                JSONArray blockArray = new JSONArray();

                for (Map.Entry<String, BlockData> entry : blocks.entrySet()) {
                    BlockData blockData = entry.getValue();
                    //&& blockData.getCpuTime() != null
                    if (blockData.getGraalID() != null ) {
                        JSONObject blockInfo = new JSONObject();
                        blockInfo.put("VtuneBlock", entry.getKey().replaceAll("Block ", ""));
                        String graalID = blockData.getGraalID();
                        if (graalID.contains("Backend Block")) {
                            blockInfo.put("GraalID", graalID.replace("Backend Block ", ""));
                            blockInfo.put("Backend Block", true);
                        } else {
                            blockInfo.put("GraalID", graalID);
                            blockInfo.put("Backend Block", false);
                        }
                        if (blockData.getCpuTime() != null) {
                            blockInfo.put("CpuTime", blockData.getCpuTime());
                            blockInfo.put("Assembler", blockData.getFormatedAsm());
                        }
                        blockArray.put(blockInfo);
                    }
                }
                resultJson.put(method, blockArray);
            } else {
                System.out.println("File not found for method: " + method);
            }
        }

        dumpResultsToJson(markerRunId, resultJson);
    }

    private static void dumpResultsToJson(String markerRunId, JSONObject resultJson) {
        // Write resultJson to a JSON file
        try (FileWriter writer = new FileWriter("Data/" + markerRunId + "/result.json")) {
            writer.write(resultJson.toString(4)); // Pretty print with an indentation of 4
        } catch (IOException e) {
            System.err.println("Error writing JSON file: " + e.getMessage());
        }
    }
}
