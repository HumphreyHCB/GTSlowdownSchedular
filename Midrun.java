import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import org.json.JSONObject;

public class Midrun {

    private static final Pattern FILE_PATTERN = Pattern.compile("_Queens::getRowColumn_(\\d+)_(\\d+)\\.json");

    public static void main(String[] args) throws IOException {
        String folderName = "Data/2024_11_06_20_19_20_SlowDown_Data";
        String methodName = "_Queens::getRowColumn";
        String outputFileName = "MidRunQueens::getRowColumnoutput.json";

        Midrun midrun = new Midrun();
        JSONObject result = midrun.processFolder(folderName, methodName);
        midrun.writeOutput(result, outputFileName);
    }

    public JSONObject processFolder(String folderName, String methodName) throws IOException {
        Map<String, File> highestBlockFiles = new HashMap<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(folderName))) {
            for (Path entry : stream) {
                Matcher matcher = FILE_PATTERN.matcher(entry.getFileName().toString());
                if (matcher.matches()) {
                    String block = matcher.group(1); // e.g., 42
                    int version = Integer.parseInt(matcher.group(2)); // e.g., 5

                    // Update to keep only the file with the highest version for each block
                    File existingFile = highestBlockFiles.get(block);
                    if (existingFile == null || version > getFileVersion(existingFile)) {
                        highestBlockFiles.put(block, entry.toFile());
                    }
                }
            }
        }

        // Aggregate data only from the highest version files for each block
        return aggregateData(highestBlockFiles);
    }

    private int getFileVersion(File file) {
        Matcher matcher = FILE_PATTERN.matcher(file.getName());
        return matcher.matches() ? Integer.parseInt(matcher.group(2)) : -1;
    }

    private JSONObject aggregateData(Map<String, File> highestBlockFiles) throws IOException {
        JSONObject result = new JSONObject();
        JSONObject backendBlocks = new JSONObject();
    
        for (File file : highestBlockFiles.values()) {
            String content = new String(Files.readAllBytes(file.toPath()));
            JSONObject jsonData = new JSONObject(content);
    
            for (String key : jsonData.keySet()) {
                Object blockDataObj = jsonData.get(key);
    
                // Check if blockData is a JSONObject, which we expect for the block structure
                if (blockDataObj instanceof JSONObject) {
                    JSONObject blockData = (JSONObject) blockDataObj;
    
                    for (String blockKey : blockData.keySet()) {
                        Object valueObj = blockData.get(blockKey);
    
                        if (blockKey.equals("Backend Blocks") && valueObj instanceof JSONObject) {
                            // Merge Backend Blocks instead of replacing
                            JSONObject innerBackendBlock = (JSONObject) valueObj;
                            for (String innerKey : innerBackendBlock.keySet()) {
                                int innerValue = innerBackendBlock.getInt(innerKey);
                                // Add or update the value in backendBlocks
                                backendBlocks.put(innerKey, innerValue);
                            }
                        } else if (valueObj instanceof Integer) {
                            // Directly replace any existing block value with the latest one
                            result.put(blockKey, valueObj);
                        }
                    }
                }
            }
        }
    
        if (!backendBlocks.isEmpty()) {
            result.put("Backend Blocks", backendBlocks);
        }
    
        return result;
    }
    

    private void writeOutput(JSONObject jsonData, String outputFileName) {
        try (FileWriter fileWriter = new FileWriter(outputFileName)) {
            fileWriter.write(jsonData.toString(4)); // Pretty print with 4-space indentation
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
