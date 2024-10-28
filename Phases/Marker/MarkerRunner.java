package Phases.Marker;

import VTune.VTuneRunner;

public class MarkerRunner {
    
    public static void run(String Benchmark) {
        
       VTuneRunner.runVtune(null, 0, false, false, null, null); 
    }

}
