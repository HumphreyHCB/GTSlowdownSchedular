
import org.json.JSONObject;

import Phases.Divining.DiviningRunner;
import Phases.Marker.MarkerRunner;
import VTune.VTuneRunner;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GTSchedular {

    public Boolean lowFootPrint;
    public String benchmark;
    public int iterations;
    public String ID;

    // Parameterized constructor
    public GTSchedular(String benchmarkString, int iterations, Boolean lowFootPrint) {
        this.benchmark = benchmarkString;
        this.iterations = iterations;
        this.lowFootPrint = lowFootPrint;
        ID = generateId();

        schedule();

    }

    /// this method should invoke both the marker and divining phase
    public void schedule() {

        MarkerRunner.run(benchmark, iterations, ID);
        //ID = "2024_10_29_18_19_36";
        DiviningRunner.run(benchmark, iterations, ID, lowFootPrint);
        // Divining


    }


    public static String generateId() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
        return now.format(formatter);
    }



}
