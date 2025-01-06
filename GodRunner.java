public class GodRunner {

    public static void main(String[] args) {
        //String benchmark = "Sieve";
        int iterations = 500;
        AWFY();
      

        

    }

    public static void AWFY() {
        int iterations = 500;

        //new GTSchedular("Towers", iterations, true, true, 2);
        //new GTSchedular("Bounce", iterations, true, true, 2);
        //new GTSchedular("Richards", iterations, true, true, 2);
        

        // new GTSchedular("List", iterations, true, true, 2);
        // new GTSchedular("Mandelbrot", iterations, true, true, 2);
        // new GTSchedular("Permute", iterations, true, true, 2);
        // new GTSchedular("Queens", iterations, true, true, 2);
        // new GTSchedular("Sieve", iterations, true, true, 2);
        // new GTSchedular("Storage", iterations, true, true, 2);
        new GTSchedular("CD", iterations, true, true, 2);
        new GTSchedular("NBody", iterations, true, true, 2);
        new GTSchedular("Json", iterations, true, true, 2);
        new GTSchedular("DeltaBlue", iterations, true, true, 2);
        new GTSchedular("Havlak", iterations, true, true, 2);

    }
    
}
 