package VTune;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class VTuneSchedular {

    public static void main(String[] args) {

        VTuneRunner runner = new VTuneRunner();

        runner.runVtune(null, 0, false, false, null, null);
        //int queensExtraArgs = getQueensExtraArgs();
    }



}
