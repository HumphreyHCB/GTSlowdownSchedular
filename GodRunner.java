import Phases.GTSchedular;

public class GodRunner {

    public static void main(String[] args) {
        //String benchmark = "Sieve";
        int iterations = 500;

        //new GTSchedular("LoopBenchmarks", 12000, true, true, 2);

        //new GTSchedular("scrabble", iterations, true, true, 1.5);
        //new GTSchedular("Mandelbrot", iterations, true, true, 2);
        //new GTSchedular("Mandelbrot", iterations, true, true, 2.5);
        //new GTSchedular("Mandelbrot", iterations, true, true, 3);
        //AWFY(2);
        
        buboAWFY(2);
        //Renaissance(2);
        
        

    }

    public static void AWFY(double slowdownAmount) {
        int iterations = 500;

        new GTSchedular("Mandelbrot", iterations, true, true, slowdownAmount);
       //new GTSchedular("Towers", iterations, true, true, slowdownAmount);
       new GTSchedular("Permute", iterations, true, true, slowdownAmount);
       new GTSchedular("NBody", iterations, true, true, slowdownAmount);
       new GTSchedular("Bounce", iterations, true, true, slowdownAmount);
       new GTSchedular("Richards", iterations, true, true, slowdownAmount);
        
        //new GTSchedular("Storage", iterations, true, false, slowdownAmount);
        new GTSchedular("List", iterations, true, true, slowdownAmount);
        new GTSchedular("Sieve", iterations, true, true, slowdownAmount);
        
        new GTSchedular("Queens", iterations, true, true, slowdownAmount);
        new GTSchedular("CD", iterations, true, true, slowdownAmount);
        new GTSchedular("Json", iterations, true, true, slowdownAmount);
        new GTSchedular("Havlak", iterations, true, true, slowdownAmount);
        //new GTSchedular("DeltaBlue", iterations, true, true, slowdownAmount); // still crashes

    }

        public static void buboAWFY(double slowdownAmount) {
        int iterations = 500;

       new GTSchedular("Mandelbrot", iterations, true, true, slowdownAmount);
       new GTSchedular("LoopBenchmarks", 12000, true, true, 2);
       new GTSchedular("NBody", iterations, true, true, slowdownAmount);
       new GTSchedular("Bounce", iterations, true, true, slowdownAmount);
       new GTSchedular("Sieve", iterations, true, true, slowdownAmount);
        
        //new GTSchedular("CD", iterations, true, true, slowdownAmount);
        //new GTSchedular("Json", iterations, true, true, slowdownAmount);


    }

    public static void Renaissance(double slowdownAmount) {
        // NOTE:
        // - For Renaissance, your runner ignores `iterations` and uses extra_args as the repetition count (-r).
        // - So this value can be anything; keep it consistent with AWFY for the constructor signature.
        int iterations = 1;
        
         new GTSchedular("mnemonics", iterations, true, true, slowdownAmount);
         new GTSchedular("scrabble", iterations, true, true, slowdownAmount);
         new GTSchedular("rx-scrabble", iterations, true, true, slowdownAmount);
         new GTSchedular("par-mnemonics", iterations, true, true, slowdownAmount);

        new GTSchedular("scala-stm-bench7", iterations, true, true, slowdownAmount); // added needs a bubo run
        //new GTSchedular("scala-doku", iterations, true, true, slowdownAmount);

        // Broken off by some error new GTSchedular("future-genetic", iterations, true, true, slowdownAmount);
        ///Broken new GTSchedular("philosophers", iterations, true, true, slowdownAmount);
        /// 
        
    }


    // Storage needs no replay
    
}
 