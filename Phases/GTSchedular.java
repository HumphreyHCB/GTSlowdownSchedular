package Phases;

import org.json.JSONObject;

import Phases.Common.CompilerReplayRunner;
import Phases.Divining.DiviningRunner;
import Phases.Divining.DiviningRunnerMultiplexed;
import Phases.Marker.MarkerRunner;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GTSchedular {

    public Boolean lowFootPrint;
    public Boolean compilerReplay;
    public String benchmark;
    public int iterations;
    public String ID;
    public double slowdownAmount;
    public String OptionalOutput;

    public static Boolean EnableBuboLIRPhase = false;

    // Parameterized constructor
    public GTSchedular(String benchmarkString, int iterations, Boolean lowFootPrint, Boolean compilerReplay,
            double slowdownAmount, Boolean enableBuboLIRPhase, String OptionalOutput) {
        this.benchmark = benchmarkString;
        this.iterations = iterations;
        this.lowFootPrint = lowFootPrint;
        this.compilerReplay = compilerReplay;
        this.slowdownAmount = slowdownAmount;
        EnableBuboLIRPhase = enableBuboLIRPhase;
        this.OptionalOutput = OptionalOutput;
        ID = generateId();

        schedule();

    }

    /// this method should invoke both the marker and divining phase
    public void schedule() {
        System.out.println("GTSchedularTimeLogger,Start," + benchmark + " " + System.currentTimeMillis());
        if (compilerReplay) {
            CompilerReplayRunner.run(benchmark, iterations, ID);
        }

        // if (compilerReplay) {

        // String sourcePath =
        // "/home/hb478/repos/GTSlowdownSchedular/FinalDataRefined100/"+ benchmark+"/"+
        // benchmark + "_CompilerReplay";
        // String destinationPath = "/home/hb478/repos/GTSlowdownSchedular/Data/" + ID +
        // "_CompilerReplay";
        // try {
        // // Ensure destination directory exists
        // Files.createDirectories(Paths.get(destinationPath));

        // // Get all files in the source directory
        // DirectoryStream<Path> stream =
        // Files.newDirectoryStream(Paths.get(sourcePath));
        // for (Path file : stream) {
        // if (Files.isRegularFile(file)) { // Only process files
        // Path destinationFile =
        // Paths.get(destinationPath).resolve(file.getFileName());
        // Files.copy(file, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        // }
        // }
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // }

        MarkerRunner.run(benchmark, iterations, ID, compilerReplay);

        DiviningRunnerMultiplexed.runComplex(benchmark, iterations, ID, lowFootPrint, compilerReplay, slowdownAmount);

        // DiviningRunnerMultiplexed.runComplexJumpStart(benchmark, iterations, ID,
        // lowFootPrint, compilerReplay, slowdownAmount);
        // Divining

        mergeFinalJsonFiles(benchmark, ID);

        System.out.println("GTSchedularTimeLogger,End," + benchmark + " " + System.currentTimeMillis());

        try {
            Paths.get(OptionalOutput);
            System.out.println("Outputting final JSON and CompilerReplay to: " + OptionalOutput);
            outputJsonandReplay(benchmark, ID, OptionalOutput);
        } catch (InvalidPathException | NullPointerException ex) {

        }

    }

    public static String generateId() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
        return now.format(formatter);
    }

    public static void mergeFinalJsonFiles(String benchmark, String id) {
        // Directory path based on the ID
        String directoryPath = "Data/" + id + "_SlowDown_Data";
        File directory = new File(directoryPath);

        // JSONObject to hold the merged JSON objects in the desired format
        JSONObject mergedData = new JSONObject();

        if (directory.exists() && directory.isDirectory()) {
            // Filter files starting with "Final_" and with ".json" extension
            File[] files = directory.listFiles((dir, name) -> name.startsWith("Final_") && name.endsWith(".json"));

            if (files != null) {
                // Process each file
                for (File file : files) {
                    try (FileReader reader = new FileReader(file)) {
                        // Read file content into a String
                        StringBuilder content = new StringBuilder();
                        int ch;
                        while ((ch = reader.read()) != -1) {
                            content.append((char) ch);
                        }

                        // Parse content as JSONObject
                        JSONObject fileData = new JSONObject(content.toString());

                        // Merge each method in fileData into mergedData
                        for (String key : fileData.keySet()) {
                            JSONObject methodData = fileData.getJSONObject(key);

                            // If method already exists in mergedData, merge its blocks
                            if (mergedData.has(key)) {
                                JSONObject existingMethodData = mergedData.getJSONObject(key);
                                mergeBlockData(existingMethodData, methodData);
                            } else {
                                // Otherwise, add the new method to mergedData
                                mergedData.put(key, methodData);
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("Error reading JSON file: " + file.getName());
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.out.println("Directory does not exist: " + directoryPath);
            return;
        }

        // Output path for the merged JSON file
        String outputPath = directoryPath + "/Final_" + benchmark + ".json";

        try (FileWriter fileWriter = new FileWriter(outputPath)) {
            // Write the merged JSONObject to the output file
            fileWriter.write(mergedData.toString(4)); // Pretty print with an indentation of 4
            System.out.println("Merged JSON data saved to: " + outputPath);
        } catch (IOException e) {
            System.err.println("Error writing merged JSON file.");
            e.printStackTrace();
        }
    }

    // Method to merge block data from newMethodData into existingMethodData
    private static void mergeBlockData(JSONObject existingMethodData, JSONObject newMethodData) {
        for (String blockKey : newMethodData.keySet()) {
            Object newBlockValue = newMethodData.get(blockKey);

            // If block already exists and is a JSONObject (like "Backend Blocks"), merge
            // recursively
            if (existingMethodData.has(blockKey) && newBlockValue instanceof JSONObject) {
                JSONObject existingBlock = existingMethodData.getJSONObject(blockKey);
                mergeBlockData(existingBlock, (JSONObject) newBlockValue);
            } else {
                // Otherwise, add or replace the block
                existingMethodData.put(blockKey, newBlockValue);
            }
        }
    }

    private static void outputJsonandReplay(String benchmark, String id, String OptionalOutput) {
        String slowdownDirectoryPath = "Data/" + id + "_SlowDown_Data";
        String replayDirectoryPath = "Data/" + id + "_CompilerReplay";
        String MarkerRunDirectoryPath = "Data/" + id + "_MarkerRun";
        

        Path slowdownDirectory = Paths.get(slowdownDirectoryPath);
        Path replayDirectory = Paths.get(replayDirectoryPath);
        Path MarkerRunDirectory = Paths.get(MarkerRunDirectoryPath);
        Path outputDirectory = Paths.get(OptionalOutput);

        try {
            Files.createDirectories(outputDirectory);

            /*
             * Find:
             * Final*<benchmark>.json
             *
             * For example:
             * Final_Bounce.json
             */
            File slowdownDirectoryFile = slowdownDirectory.toFile();

            File[] finalJsonFiles = slowdownDirectoryFile.listFiles(
                    (dir, name) -> name.startsWith("Final") &&
                            name.endsWith(benchmark + ".json"));

            if (finalJsonFiles != null && finalJsonFiles.length > 0) {
                for (File jsonFile : finalJsonFiles) {
                    Path destination = outputDirectory.resolve(jsonFile.getName());

                    Files.copy(
                            jsonFile.toPath(),
                            destination,
                            StandardCopyOption.REPLACE_EXISTING);

                    System.out.println(
                            "Copied final JSON: " +
                                    jsonFile.getPath() +
                                    " -> " +
                                    destination);
                }
            } else {
                System.err.println(
                        "Could not find Final*" +
                                benchmark +
                                ".json in " +
                                slowdownDirectoryPath);
            }


            /*
            * Copy MarkerPhaseInfo.json from:
            *
            * Data/<id>_MarkerRun/MarkerPhaseInfo.json
            *
            * into OptionalOutput.
            */
            Path markerPhaseInfo = MarkerRunDirectory.resolve("MarkerPhaseInfo.json");

            if (Files.exists(markerPhaseInfo) && Files.isRegularFile(markerPhaseInfo)) {
                Path destination = outputDirectory.resolve("MarkerPhaseInfo.json");

                Files.copy(
                        markerPhaseInfo,
                        destination,
                        StandardCopyOption.REPLACE_EXISTING);

                System.out.println(
                        "Copied MarkerPhaseInfo.json: " +
                                markerPhaseInfo +
                                " -> " +
                                destination);
            } else {
                System.err.println(
                        "MarkerPhaseInfo.json does not exist: " +
                                markerPhaseInfo);
            }

            /*
             * Copy the entire CompilerReplay directory.
             *
             * Output becomes:
             *
             * OptionalOutput/
             * Final_Bounce.json
             * <id>_CompilerReplay/
             * ...
             */
            if (Files.exists(replayDirectory) && Files.isDirectory(replayDirectory)) {

                Path replayOutputDirectory = outputDirectory.resolve(id + "_CompilerReplay");

                try (var paths = Files.walk(replayDirectory)) {
                    paths.forEach(source -> {
                        try {
                            Path relativePath = replayDirectory.relativize(source);
                            Path destination = replayOutputDirectory.resolve(relativePath);

                            if (Files.isDirectory(source)) {
                                Files.createDirectories(destination);
                            } else {
                                Files.createDirectories(destination.getParent());

                                Files.copy(
                                        source,
                                        destination,
                                        StandardCopyOption.REPLACE_EXISTING);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(
                                    "Failed copying: " + source,
                                    e);
                        }
                    });
                }

                System.out.println(
                        "Copied CompilerReplay directory: " +
                                replayDirectory +
                                " -> " +
                                replayOutputDirectory);

            } else {
                System.err.println(
                        "CompilerReplay directory does not exist: " +
                                replayDirectoryPath);
            }

            /*
 * Copy MarkerPhaseInfo.json from:
 *
 * Data/<id>_MarkerRun/MarkerPhaseInfo.json
 *
 * into OptionalOutput.
 */
//Path markerPhaseInfo = MarkerRunDirectory.resolve("MarkerPhaseInfo.json");

if (Files.exists(markerPhaseInfo) && Files.isRegularFile(markerPhaseInfo)) {
    Path destination = outputDirectory.resolve("MarkerPhaseInfo.json");

    Files.copy(
            markerPhaseInfo,
            destination,
            StandardCopyOption.REPLACE_EXISTING);

    System.out.println(
            "Copied MarkerPhaseInfo.json: " +
                    markerPhaseInfo +
                    " -> " +
                    destination);
} else {
    System.err.println(
            "MarkerPhaseInfo.json does not exist: " +
                    markerPhaseInfo);
}


/*
 * Copy MarkerPhase_BuboIncluded.json if it exists.
 */
Path markerPhaseBuboIncluded =
        MarkerRunDirectory.resolve("MarkerPhase_BuboIncluded.json");

if (Files.exists(markerPhaseBuboIncluded)
        && Files.isRegularFile(markerPhaseBuboIncluded)) {

    Path destination =
            outputDirectory.resolve("MarkerPhase_BuboIncluded.json");

    Files.copy(
            markerPhaseBuboIncluded,
            destination,
            StandardCopyOption.REPLACE_EXISTING);

    System.out.println(
            "Copied MarkerPhase_BuboIncluded.json: " +
                    markerPhaseBuboIncluded +
                    " -> " +
                    destination);

} else {
    System.out.println(
            "MarkerPhase_BuboIncluded.json does not exist, skipping: " +
                    markerPhaseBuboIncluded);
}

        } catch (IOException e) {
            System.err.println(
                    "Error copying final JSON / CompilerReplay output.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        mergeFinalJsonFiles("deltablue", "2025_03_20_10_34_07");
    }

}
