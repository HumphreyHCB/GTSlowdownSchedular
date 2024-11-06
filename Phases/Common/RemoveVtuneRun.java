package Phases.Common;

import java.io.File;

public class RemoveVtuneRun {

    public static void run(String runLocation) {
        // Create a File object for the specified location
        File folder = new File(runLocation);
        
        // Check if the folder exists
        if (folder.exists()) {
            // Call the recursive delete function
            deleteRecursively(folder);
        } else {
            System.out.println("Folder not found: " + runLocation);
        }
    }

    private static void deleteRecursively(File file) {
        // If the file is a directory, delete its contents recursively
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) { // Check to avoid NullPointerException
                for (File subFile : files) {
                    deleteRecursively(subFile);
                }
            }
        }
        // Delete the file or empty directory
        file.delete();
    }
}
