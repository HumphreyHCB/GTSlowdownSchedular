package Tests;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import Phases.Divining.DiviningRunner;
import Phases.Marker.MarkerRunner;

public class CDConsistencyTest {

    public static void main(String[] args) {
        int iterations = 500;
        boolean lowFootPrint = true;
        String benchmark = "CD";
        String ID = generateId();

        MarkerRunner.run(benchmark, iterations, ID);
        VTuneFileComparatorRunner.CheckCD(ID);
        //MarkerRunner.run(benchmark, iterations, generateId());
        //MarkerRunner.run(benchmark, iterations, generateId());
        // ID = "2024_10_29_18_19_36";

    

    }

    public static String generateId() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
        return now.format(formatter);
    }
    
}