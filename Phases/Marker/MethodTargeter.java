package Phases.Marker;


import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
/**
 * MethodTargeter
 */
public class MethodTargeter {

    static double SIGNIFICANT_THRESHOLD = 85.0;

    public static List<String> findSignificantMethod(Map<String, Double> methods) {
        // Calculate the total execution time
        double totalExecutionTime = 0.0;
        for (double time : methods.values()) {
            totalExecutionTime += time;
        }
    
        // Determine the dynamic threshold based on the given percentage of the total execution time
        double threshold = (SIGNIFICANT_THRESHOLD / 100.0) * totalExecutionTime;
    
        // Sort the methods map by value in descending order without using streams
        List<Entry<String, Double>> methodList = new ArrayList<>(methods.entrySet());
        methodList.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));
    
        List<String> significantMethods = new ArrayList<>();
        double sum = 0.0;
    
        // Iterate over the sorted methods and add method names until the sum is at least the calculated threshold
        // for (Entry<String, Double> entry : methodList) {
        //     significantMethods.add(entry.getKey());
        //     sum += entry.getValue();
    
        //     if (sum >= threshold) {
        //         break;
        //     }
        // }

        for (Entry<String, Double> entry : methodList) {
            if (entry.getValue() < 3) {
                break;
            }

            significantMethods.add(entry.getKey());
            sum += entry.getValue();
    
        }

        // Remove "Interpreter" from methodList
        methodList.removeIf(entry -> entry.getKey().equals("Interpreter"));
        methodList.removeIf(entry -> entry.getKey().startsWith("G1"));
    
        return significantMethods;
    }
}
