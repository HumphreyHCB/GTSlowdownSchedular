package Phases.Common;

import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SlowdownFileRetriever {


    public static void main(String[] args) {
        try {
            loadMethodBlockCostsFromJSON("/home/hb478/repos/GTSlowdownSchedular/FinalDataRefined100/Havlak/Final_Havlak.json");
        } catch (IOException e) {
            System.out.println("Failed to load method block costs: " + e.getMessage());
        }

        System.out.println(getBackendBlockCost("havlak.HavlakLoopFinder.lambda$stepEProcessNonBackPreds$1", 318));
    }


    /**
     * For normal method blocks:
     *   methodName -> (vtuneBlockNumber -> cost)
     */
    private static final Map<String, Map<Integer, Integer>> METHOD_VTUNE_BLOCK_COST_MAP = new HashMap<>();

    /**
     * For backend blocks:
     *   methodName -> (vtuneBlockNumber -> cost)
     */
    private static final Map<String, Map<Integer, Integer>> BACKEND_BLOCK_COST_MAP = new HashMap<>();

    // Regex to capture the number after "(Vtune Block ...)"
    private static final Pattern VTUNE_BLOCK_PATTERN = Pattern.compile(".*\\(Vtune Block\\s*(\\d+)\\).*");


    /**
     * Reads the JSON file, parses each method entry, and extracts the Vtune block and cost.
     * Example snippet:
     *
     * {
     *   "havlak.HavlakLoopFinder.lambda$stepEProcessNonBackPreds$1": {
     *       "3 (Vtune Block 5)": 5,
     *       "98 (Vtune Block 145)": 6,
     *       "150 (Vtune Block 26)": 7,
     *       "Backend Blocks": {
     *           "99 (Vtune Block 500)": 20,
     *           "102 (Vtune Block 501)": 15
     *       }
     *   },
     *   ...
     * }
     */
    public static void loadMethodBlockCostsFromJSON(String filePath) throws IOException {
        if (!Files.exists(Paths.get(filePath))) {
            System.out.println("Could not locate " + filePath);
            System.out.println("Skipping loading (this might cause a fatal crash if GTSlowdown is on)");
            return;
        }

        String jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));
        JSONObject root;
        try {
            root = new JSONObject(jsonContent);
        } catch (JSONException e) {
            throw new IOException("Failed to parse JSON: " + e.getMessage(), e);
        }

        // For each methodName in the root
        for (String methodName : root.keySet()) {
            // Value is expected to be a JSONObject with block keys
            JSONObject methodBlocks = root.optJSONObject(methodName);
            if (methodBlocks == null) {
                continue; // skip if it's not a valid JSON object
            }

            // We'll store normal blocks here
            Map<Integer, Integer> vtuneCostMap = new HashMap<>();
            // We'll store backend blocks here
            Map<Integer, Integer> backendVtuneCostMap = new HashMap<>();

            // For each key in methodBlocks, we have either "NN (Vtune Block MM)" or "Backend Blocks"
            for (String blockKey : methodBlocks.keySet()) {
                // If it's "Backend Blocks", parse those separately
                if ("Backend Blocks".equals(blockKey)) {
                    JSONObject backendObj = methodBlocks.optJSONObject(blockKey);
                    if (backendObj != null) {
                        for (String backendKey : backendObj.keySet()) {
                            int cost = backendObj.optInt(backendKey, 0);
                            int vtuneBlockNum = extractVtuneBlockNumber(backendKey);
                            if (vtuneBlockNum != -1) {
                                backendVtuneCostMap.put(vtuneBlockNum, cost);
                            }
                        }
                    }
                } else {
                    // It's a normal block entry "NN (Vtune Block MM)": cost
                    int cost = methodBlocks.optInt(blockKey, 0);
                    int vtuneBlockNum = extractVtuneBlockNumber(blockKey);
                    if (vtuneBlockNum != -1) {
                        vtuneCostMap.put(vtuneBlockNum, cost);
                    }
                }
            }

            // Store in the main maps
            METHOD_VTUNE_BLOCK_COST_MAP.put(methodName, vtuneCostMap);
            BACKEND_BLOCK_COST_MAP.put(methodName, backendVtuneCostMap);
        }
    }

    /**
     * Extracts the integer that appears after "(Vtune Block ...)".
     * Returns -1 if not found or cannot parse.
     */
    private static int extractVtuneBlockNumber(String key) {
        Matcher matcher = VTUNE_BLOCK_PATTERN.matcher(key);
        if (matcher.matches()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        return -1;
    }

    /**
     * Returns the cost associated with a given methodName and Vtune block (for normal blocks).
     *
     * For example, if the JSON has:
     *   "32 (Vtune Block 129)": 14
     * The methodName's map will have (129 -> 14).
     */
    public static int getBlockCost(String methodName, int vtuneBlockNumber) {
        // Optionally strip parentheses, e.g. "foo(int)" -> "foo"
        int index = methodName.indexOf('(');
        if (index != -1) {
            methodName = methodName.substring(0, index).trim();
        }

        Map<Integer, Integer> blockCostMap = METHOD_VTUNE_BLOCK_COST_MAP.get(methodName);
        if (blockCostMap != null) {
            Integer cost = blockCostMap.get(vtuneBlockNumber);
            if (cost != null) {
                return cost;
            }
        }
        return 0;
    }

    /**
     * Returns the cost associated with a given methodName and Vtune block (for backend blocks).
     *
     * For example, if under "Backend Blocks" you have:
     *   "99 (Vtune Block 500)": 20
     * Then the methodName's backend map will have (500 -> 20).
     */
    public static int getBackendBlockCost(String methodName, int vtuneBlockNumber) {
        // Optionally strip parentheses, e.g. "foo(int)" -> "foo"
        int index = methodName.indexOf('(');
        if (index != -1) {
            methodName = methodName.substring(0, index).trim();
        }

        Map<Integer, Integer> backendCostMap = BACKEND_BLOCK_COST_MAP.get(methodName);
        if (backendCostMap != null) {
            Integer cost = backendCostMap.get(vtuneBlockNumber);
            if (cost != null) {
                return cost;
            }
        }
        return 0;
    }
}
