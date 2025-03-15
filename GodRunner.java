public class GodRunner {

    public static void main(String[] args) {
        //String benchmark = "Sieve";
        int iterations = 500;
        AWFY(1);
        //new GTSchedular("Havlak", iterations, true, true, 2);
        //new GTSchedular("Havlak", iterations, true, true, 2.5);

        //new GTSchedular("List", iterations, true, true, 2);
      

        

    }

    public static void AWFY(double slowdownAmount) {
        int iterations = 500;

       new GTSchedular("Towers", iterations, true, true, slowdownAmount);
       new GTSchedular("Permute", iterations, true, true, slowdownAmount);
       new GTSchedular("NBody", iterations, true, true, slowdownAmount);
       new GTSchedular("Bounce", iterations, true, true, slowdownAmount);
       new GTSchedular("Richards", iterations, true, true, slowdownAmount);
        

        new GTSchedular("List", iterations, true, true, slowdownAmount);
        new GTSchedular("Mandelbrot", iterations, true, true, slowdownAmount);
        new GTSchedular("Permute", iterations, true, true, slowdownAmount);
        new GTSchedular("Queens", iterations, true, true, slowdownAmount);
        new GTSchedular("Storage", iterations, true, true, slowdownAmount);
        new GTSchedular("CD", iterations, true, true, slowdownAmount);
        new GTSchedular("DeltaBlue", iterations, true, true, slowdownAmount);
        new GTSchedular("Json", iterations, true, true, slowdownAmount);
        new GTSchedular("Sieve", iterations, true, true, slowdownAmount);
        new GTSchedular("Havlak", iterations, true, true, slowdownAmount);

    }
    
}
 