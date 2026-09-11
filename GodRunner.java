import Phases.GTSchedular;

public class GodRunner {

    public static void main(String[] args) {
        if (args.length > 0 && "--ecosystem-divine".equals(args[0])) {
            runEcosystemDivine(args);
            return;
        }

        //String benchmark = "Sieve";
        int iterations = 300;

       // new GTSchedular("LoopBenchmarks", 12000, true, true, 2);

        //new GTSchedular("scrabble", iterations, true, true, 1.5);
        //new GTSchedular("Mandelbrot", iterations, true, true, 2);
        //new GTSchedular("Mandelbrot", iterations, true, true, 2.5);
        new GTSchedular("Mandelbrot", iterations, true, true, 3,true,"");
        //new GTSchedular("Bounce", iterations, true, true, 2, false,"");
        //AWFY(2);
        //new GTSchedular("Mandelbrot", iterations, true, true, 2);
        //buboAWFY(2);
        //Renaissance(2);

        //new GTSchedular("InnerLoopStepBenchmark", iterations, true, true, 2);
        
        

    }

    public static void AWFY(double slowdownAmount) {
        int iterations = 500;

        new GTSchedular("Mandelbrot", iterations, true, true, slowdownAmount, false,"");
       //new GTSchedular("Towers", iterations, true, true, slowdownAmount);
       new GTSchedular("Permute", iterations, true, true, slowdownAmount, false,"");
       new GTSchedular("NBody", iterations, true, true, slowdownAmount, false,"");
       new GTSchedular("Bounce", iterations, true, true, slowdownAmount, false,"");
       new GTSchedular("Richards", iterations, true, true, slowdownAmount, false,"");
        
        //new GTSchedular("Storage", iterations, true, false, slowdownAmount);
        new GTSchedular("List", iterations, true, true, slowdownAmount, false,"");
        new GTSchedular("Sieve", iterations, true, true, slowdownAmount, false,"");
        
        new GTSchedular("Queens", iterations, true, true, slowdownAmount, false,"");
        new GTSchedular("CD", iterations, true, true, slowdownAmount, false,"");
        new GTSchedular("Json", iterations, true, true, slowdownAmount, false,"");
        new GTSchedular("Havlak", iterations, true, true, slowdownAmount, false,"");
        //new GTSchedular("DeltaBlue", iterations, true, true, slowdownAmount); // still crashes

    }

        public static void buboAWFY(double slowdownAmount) {
        int iterations = 500;

       new GTSchedular("Mandelbrot", iterations, true, true, slowdownAmount, true,"");
       new GTSchedular("LoopBenchmarks", 12000, true, true, 2, true,"");
       new GTSchedular("NBody", iterations, true, true, slowdownAmount, true,"");
       new GTSchedular("Bounce", iterations, true, true, slowdownAmount, true,"");
       new GTSchedular("Sieve", iterations, true, true, slowdownAmount, true,"");
        
        new GTSchedular("CD", iterations, true, true, slowdownAmount, true,"");
        new GTSchedular("Json", iterations, true, true, slowdownAmount, true,"");


    }

    public static void Renaissance(double slowdownAmount) {
        // NOTE:
        // - For Renaissance, your runner ignores `iterations` and uses extra_args as the repetition count (-r).
        // - So this value can be anything; keep it consistent with AWFY for the constructor signature.
        int iterations = 1;
        
         new GTSchedular("mnemonics", iterations, true, true, slowdownAmount, false,"");
         new GTSchedular("scrabble", iterations, true, true, slowdownAmount, false,"");
         new GTSchedular("rx-scrabble", iterations, true, true, slowdownAmount, false,"");
         new GTSchedular("par-mnemonics", iterations, true, true, slowdownAmount, false,"");

        new GTSchedular("scala-stm-bench7", iterations, true, true, slowdownAmount, false,""); // added needs a bubo run
        //new GTSchedular("scala-doku", iterations, true, true, slowdownAmount);

        // Broken off by some error new GTSchedular("future-genetic", iterations, true, true, slowdownAmount);
        ///Broken new GTSchedular("philosophers", iterations, true, true, slowdownAmount);
        /// 
        
    }


    private static void runEcosystemDivine(String[] args) {
    if (args.length != 6) {
        System.err.println(
                "Usage: GodRunner --ecosystem-divine " +
                "<benchmark> <iterations> <slowdown> <enable-bubo-lir> <output-directory>");
        System.exit(2);
    }

    String benchmark = args[1];
    int iterations;
    double slowdownAmount;
    boolean enableBuboLIRPhase;
    String outputDirectory = args[5];

    try {
        iterations = Integer.parseInt(args[2]);
        slowdownAmount = Double.parseDouble(args[3]);
    } catch (NumberFormatException exception) {
        System.err.println("Iterations and slowdown must be valid numbers.");
        System.exit(2);
        return;
    }

    if (!"true".equalsIgnoreCase(args[4]) &&
            !"false".equalsIgnoreCase(args[4])) {
        System.err.println(
                "enable-bubo-lir must be either true or false.");
        System.exit(2);
    }

    enableBuboLIRPhase = Boolean.parseBoolean(args[4]);

    if (benchmark.isBlank()) {
        System.err.println("Benchmark name cannot be empty.");
        System.exit(2);
    }

    if (iterations <= 0) {
        System.err.println("Iterations must be greater than zero.");
        System.exit(2);
    }

    if (slowdownAmount <= 0) {
        System.err.println("Slowdown amount must be greater than zero.");
        System.exit(2);
    }

    if (outputDirectory.isBlank()) {
        System.err.println("Output directory cannot be empty.");
        System.exit(2);
    }

    System.out.println("Starting ecosystem Divine run");
    System.out.println("Benchmark: " + benchmark);
    System.out.println("Iterations: " + iterations);
    System.out.println("Slowdown: " + slowdownAmount);
    System.out.println("Low footprint: true");
    System.out.println("Compiler replay: true");
    System.out.println("BuboLIRPhase: " + enableBuboLIRPhase);
    System.out.println("Output: " + outputDirectory);

    new GTSchedular(
            benchmark,
            iterations,
            true,
            true,
            slowdownAmount,
            enableBuboLIRPhase,
            outputDirectory);
}

    // Storage needs no replay
    
}
 