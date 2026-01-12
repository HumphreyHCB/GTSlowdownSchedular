package GTResources;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AWFYBenchmarksLookUp {

    private static final String FILE_PATH =
            "/home/hb478/repos/GTSlowdownSchedular/GTResources/AWFY_Benchmarks.json";

    private static final String AWFY_GROUP = "AWFY Benchmarks";
    private static final String RENAISSANCE_GROUP = "Renaissance Benchmarks";

    private static JSONObject loadJson() throws IOException {
        String jsonString = new String(Files.readAllBytes(Paths.get(FILE_PATH)));
        return new JSONObject(jsonString);
    }

    /**
     * Returns true iff benchmarkName is in the "Renaissance Benchmarks" group.
     */
    public static boolean isRenaissanceBenchmark(String benchmarkName) {
        try {
            JSONObject root = loadJson();
            if (!root.has(RENAISSANCE_GROUP)) {
                return false;
            }
            return root.getJSONObject(RENAISSANCE_GROUP).has(benchmarkName);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Finds the benchmark entry (JSONObject) by searching:
     *  1) AWFY Benchmarks
     *  2) Renaissance Benchmarks
     *
     * Returns null if not found.
     */
    private static JSONObject findBenchmarkEntry(JSONObject root, String benchmarkName) {
        if (root.has(AWFY_GROUP)) {
            JSONObject awfy = root.getJSONObject(AWFY_GROUP);
            if (awfy.has(benchmarkName)) {
                return awfy.getJSONObject(benchmarkName);
            }
        }

        if (root.has(RENAISSANCE_GROUP)) {
            JSONObject ren = root.getJSONObject(RENAISSANCE_GROUP);
            if (ren.has(benchmarkName)) {
                return ren.getJSONObject(benchmarkName);
            }
        }

        return null;
    }

    public static boolean getIfSingleFile(String benchmarkName) {
        try {
            JSONObject root = loadJson();
            JSONObject entry = findBenchmarkEntry(root, benchmarkName);

            if (entry == null) {
                System.out.println("Benchmark not found: " + benchmarkName);
                return false;
            }

            return entry.getBoolean("SingleFile");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int getExtraArgs(String benchmarkName) {
        try {
            JSONObject root = loadJson();
            JSONObject entry = findBenchmarkEntry(root, benchmarkName);

            if (entry == null) {
                System.out.println("Benchmark not found: " + benchmarkName);
                return -1;
            }

            return entry.getInt("extra_args");
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
