package Tests;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class VTuneMarkerFilter {

    private static PrintWriter logWriter;

    public static void setLogWriter(PrintWriter w) {
        logWriter = w;
    }

    public static void filterAndDetectBlocks(String inputFilePath, String outputFilePath) throws IOException {
        Pattern markerPattern = Pattern.compile("(vpblendd xmm0, xmm0, xmm0,|sfence)");
        String blockRegex = ".*(?<!<)Block\\s+\\d+(?!>).*";
        List<String> filteredLines = new ArrayList<>();

        String previousLine = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (markerPattern.matcher(line).find()) {
                    continue;
                }

                if (previousLine != null && previousLine.matches(blockRegex) && line.matches(blockRegex)) {
                    if (logWriter != null) {
                        logWriter.println("Consecutive blocks detected:");
                        logWriter.println(previousLine);
                        logWriter.println(line);
                    } else {
                        System.out.println("Consecutive blocks detected:");
                        System.out.println(previousLine);
                        System.out.println(line);
                    }
                }

                filteredLines.add(line);
                previousLine = line;
            }

            for (String filteredLine : filteredLines) {
                writer.write(filteredLine);
                writer.newLine();
            }
        }
    }
}
