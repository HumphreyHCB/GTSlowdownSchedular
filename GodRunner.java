import Phases.GTSchedular;

public class GodRunner {

    public static void main(String[] args) {
        //String benchmark = "Sieve";
        int iterations = 500;
        //new GTSchedular("DeltaBlue", iterations, true, true, 1.5);
       // new GTSchedular("DeltaBlue", iterations, true, true, 2);
        //new GTSchedular("Json", iterations, true, true, 2.5);
        //AWFY(1);
        //new GTSchedular("Havlak", iterations, true, true, 2);
        //new GTSchedular("Havlak", iterations, true, true, 2.5);

        //new GTSchedular("List", iterations, true, true, 1.5);
        //new GTSchedular("List", iterations, true, true, 2.5);

        System.out.println("Start Time : " + System.currentTimeMillis());

        //new GTSchedular("Towers", iterations, true, true, 2, "MOV");
        //new GTSchedular("Towers", iterations, true, true, 2, "NOP");
        //new GTSchedular("Towers", iterations, true, true, 2, "SFENCE");
        //new GTSchedular("Towers", iterations, true, true, 2, "PAUSE", "A");
        //new GTSchedular("Towers", iterations, true, true, 2, "PP", "A");


        //new GTSchedular("Towers", iterations, true, true, 2, "MOV", "B");
        //new GTSchedular("Towers", iterations, true, true, 2, "NOP", "B");
        //new GTSchedular("Towers", iterations, true, true, 2, "SFENCE", "B");
        //new GTSchedular("Towers", iterations, true, true, 2, "PAUSE", "B");
        //new GTSchedular("Towers", iterations, true, true, 2, "PP", "B");

        new GTSchedular("Towers", iterations, true, true, 2);
        new GTSchedular("Towers", iterations, true, true, 2);
        new GTSchedular("Towers", iterations, true, true, 2);

        System.out.println("End Time : " + System.currentTimeMillis());
        

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
 