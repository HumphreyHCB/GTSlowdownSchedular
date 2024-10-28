# Graal Compiler Slowdown Analysis

Repository for analyzing the slowdown of the Graal compiler using GT and for running VTune analysis.

This project leverages data from both GT and VTune to accurately measure slowdown for given benchmarks.

## File Structure

### GTResources
Contains AWFY benchmark names and their `ExtraArgs`. A static class is provided for easy lookup.

### Phases
Contains folders and files related to the marker phases and the Divining phase.

### VTune
Includes files for running and extracting data from VTune reports.

### Data
Folder containing all data generated throughout a run.
