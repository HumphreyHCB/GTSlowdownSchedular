package Phases.Marker;


import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
/**
 * MethodTargeter
 */
/**
 * MethodTargeter
 */
public class MethodTargeter {

    static double SIGNIFICANT_THRESHOLD = 90.0;

    public static List<String> findSignificantMethod(Map<String, Double> methods) {
        
        // 1. Calculate total execution time
        double totalExecutionTime = 0.0;
        for (double time : methods.values()) {
            totalExecutionTime += time;
        }

        // 2. Remove unwanted methods:
        //    - "Interpreter"
        //    - Methods starting with "G1"
        methods.entrySet().removeIf(e -> e.getKey().equals("Interpreter") || e.getKey().startsWith("G1"));

        // 3. Determine the threshold
        double threshold = (SIGNIFICANT_THRESHOLD / 100.0) * totalExecutionTime;

        // 4. Sort the remaining methods in descending order by their runtime
        List<Map.Entry<String, Double>> sortedMethods = new ArrayList<>(methods.entrySet());
        sortedMethods.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));

        // 5. Accumulate until we reach or exceed the threshold
        double sum = 0.0;
        List<String> significantMethods = new ArrayList<>();
        for (Map.Entry<String, Double> entry : sortedMethods) {
            if (entry.getValue() < 0.9) {
                continue;
                
            }
            sum += entry.getValue();
            significantMethods.add(entry.getKey());
            if (sum >= threshold) {
                break;
            }
        }

        return significantMethods;
    }
}


