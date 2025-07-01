package ECOOPaperData.Testers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import VTune.VTuneAnalyzer;


public class CompareTwoRuns {
    public static void compare(String NormalRunPath, String SlowdownRunPath, List<String> methodsToCheck, String logFilePath) {
        // Compare the two runs

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath))) {
            double totalPercentageIncrease = 0;
            int totalBlocks = 0;
            double totalNormalTime = 0;
            double totalSlowdownTime = 0;

            for (String method : methodsToCheck) {
                
                Map<Integer, Double> NormalRunBlocks =  VTuneAnalyzer.getCpuTimesForAllBlocks(NormalRunPath, method);
                Map<Integer, Double> SlowdownBlocks =  VTuneAnalyzer.getCpuTimesForAllBlocks(SlowdownRunPath, method);

                List<Double> percentageIncreases = new ArrayList<>();
                int methodBlocks = 0;

                Set<Integer> allBlockIds = new HashSet<>(NormalRunBlocks.keySet());
                allBlockIds.addAll(SlowdownBlocks.keySet());

                for (Integer blockId : allBlockIds) {
                    double normalTime = NormalRunBlocks.getOrDefault(blockId, 0.0);
                    double slowdownTime = SlowdownBlocks.getOrDefault(blockId, 0.0);

                    totalNormalTime += normalTime;
                    totalSlowdownTime += slowdownTime;

                    double percentageIncrease = 0;
                    if (normalTime > 0) {
                        percentageIncrease = ((slowdownTime - normalTime) / normalTime) * 100;
                    }

                    if (percentageIncrease > 0) {
                        percentageIncreases.add(percentageIncrease);
                    }

                    methodBlocks++;

                    writer.write("Method: " + method + ", Block ID: " + blockId + ", Normal Time: " + normalTime + ", Slowdown Time: " + slowdownTime + ", Percentage Increase: " + percentageIncrease + "%\n");
                }

                Collections.sort(percentageIncreases);
                double medianMethodPercentageIncrease = 0;
                if (!percentageIncreases.isEmpty()) {
                    int middle = percentageIncreases.size() / 2;
                    if (percentageIncreases.size() % 2 == 0) {
                        medianMethodPercentageIncrease = (percentageIncreases.get(middle - 1) + percentageIncreases.get(middle)) / 2.0;
                    } else {
                        medianMethodPercentageIncrease = percentageIncreases.get(middle);
                    }
                }

                totalPercentageIncrease += medianMethodPercentageIncrease;
                totalBlocks++;

                writer.write("Method: " + method + ", Median Percentage Increase: " + medianMethodPercentageIncrease + "%\n");
            }

            double averageTotalPercentageIncrease = totalPercentageIncrease / totalBlocks;
            writer.write("Total Average Percentage Increase: " + averageTotalPercentageIncrease + "%\n");

            double totalPercentageIncreaseOverall = 0;
            if (totalNormalTime > 0) {
                totalPercentageIncreaseOverall = ((totalSlowdownTime - totalNormalTime) / totalNormalTime) * 100;
            }
            writer.write("Total Normal Time: " + totalNormalTime + "\n");
            writer.write("Total Slowdown Time: " + totalSlowdownTime + "\n");
            writer.write("Total Percentage Increase Overall: " + totalPercentageIncreaseOverall + "%\n");
        

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}