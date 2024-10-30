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

    static double SIGNIFICANT_THRESHOLD = 80.0;

    public static List<String> findSignificantMethod(Map<String, Double> methods) {
        // Sort the methods map by value in descending order without using streams
        List<Entry<String, Double>> methodList = new ArrayList<>(methods.entrySet());
        methodList.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));

        List<String> significantMethods = new ArrayList<>();
        double sum = 0.0;

        // Iterate over the sorted methods and add method names until the sum is at least 95
        for (Entry<String, Double> entry : methodList) {
            significantMethods.add(entry.getKey());
            sum += entry.getValue();

            if (sum >= SIGNIFICANT_THRESHOLD) {
                break;
            }
        }

        return significantMethods;
    }
}
