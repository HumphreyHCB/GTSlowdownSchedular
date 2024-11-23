package Phases.Common;

import GTResources.AWFYBenchmarksLookUp;
import VTune.VTuneAnalyzer;
import VTune.VTuneRunner;
import VTune.VTuneReportRipper;
import VTune.VTuneReportRipper.BlockData;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

public class BuildResultsForRun {

    public static void run(String markerRunId, List<String> significantMethods, boolean includeLineCount) {
        if (significantMethods == null || significantMethods.isEmpty()) {
            System.out.println("No significant methods provided.");
            return;
        }

        for (String method : significantMethods) {
            String outputFilePath = String.format("/home/hb478/repos/GTSlowdownSchedular/Data/%s/%s.txt", markerRunId,
                    method.replaceAll("[\\/:*?\"<>|]", "_"));
            VTuneAnalyzer.generateMethodBlockVTuneReport(markerRunId, method, outputFilePath);
        }

        processGeneratedFiles(markerRunId, significantMethods, includeLineCount);
    }

    private static void processGeneratedFiles(String markerRunId, List<String> significantMethods, boolean includeLineCount) {
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
                    if (blockData.getCpuTime() != null) { // Only include blocks that have CPU time
                        JSONObject blockInfo = new JSONObject();
                        blockInfo.put("VtuneBlock", entry.getKey().replaceAll("Block ", ""));
                        blockInfo.put("CpuTime", blockData.getCpuTime());
                        blockArray.put(blockInfo);
                        if (includeLineCount) {
                            blockInfo.put("Assembler",entry.getValue().getFormatedAsm());
                        }
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
