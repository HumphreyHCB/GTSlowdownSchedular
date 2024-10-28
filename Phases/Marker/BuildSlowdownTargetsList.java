package Phases.Marker;

import GTResources.AWFYBenchmarksLookUp;
import VTune.VTuneAnalyzer;
import VTune.VTuneRunner;
import VTune.VTuneReportRipper;
import VTune.VTuneReportRipper.BlockData;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

public class BuildSlowdownTargetsList {

    public static void run(String markerRunId, List<String> significantMethods) {
        if (significantMethods == null || significantMethods.isEmpty()) {
            System.out.println("No significant methods provided.");
            return;
        }

        for (String method : significantMethods) {
            String outputFilePath = String.format("/home/hb478/repos/GTSlowdownSchedular/Data/%s/%s.txt", markerRunId, method.replaceAll("[\\/:*?\"<>|]", "_"));
            VTuneAnalyzer.generateMethodBlockVTuneReport(markerRunId, method, outputFilePath);
        }

        processGeneratedFiles(markerRunId, significantMethods);
    }

    private static void processGeneratedFiles(String markerRunId, List<String> significantMethods) {
        String directoryPath = String.format("/home/hb478/repos/GTSlowdownSchedular/Data/%s/", markerRunId);
        VTuneReportRipper ripper = new VTuneReportRipper();

        for (String method : significantMethods) {
            String filePath = directoryPath + method.replaceAll("[\\/:*?\"<>|]", "_") + ".txt";
            File file = new File(filePath);
            if (file.exists()) {
                Map<String, BlockData> blocks = ripper.processFileIntoBlocks(filePath);

                // Output blocks and their Graal ID for verification
                System.out.println("Method: " + method);
                for (Map.Entry<String, BlockData> entry : blocks.entrySet()) {
                    System.out.println("Block: " + entry.getKey());
                    if (entry.getValue().getGraalID() != null) {
                        System.out.println("Graal ID: " + entry.getValue().getGraalID());
                    }
                    System.out.println();
                }
            } else {
                System.out.println("File not found for method: " + method);
            }
        }
    }

}
