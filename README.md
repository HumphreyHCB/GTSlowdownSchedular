# Graal Compiler Slowdown Analysis

Repository for analyzing the slowdown of the Graal compiler using GT and for running VTune analysis.

This project leverages data from both GT and VTune to accurately measure slowdown for given benchmarks.

## File Structure

### GTResources
Contains AWFY benchmark names and their `ExtraArgs`. A static class is provided for easy lookup.

### Phases
Contains folders and files related to the marker phases and the Divining phase.

- **Common**  
  Common classes that both phases use.

- **Marker**  
  In this phase, we will perform at least two runs. The first is a "normal" run—this is a VTune-attached run with no marking or slowdown applied.  
  Then, we will conduct an additional marker run. This will use Graal's "mark basic blocks" phase to tag all blocks identified by Graal.

- **Divining**  
  This is the main phase of the GT schedule. It will systematically determine the correct amount of slowdown for every VTune block and produce a final data file. This file can then be passed to an executor to generate a slowed-down version of the program.

### VTune
Includes files for running and extracting data from VTune reports.

### Data
Folder containing all data generated throughout a run.

### Tests
Contains tests to verify if the Divining phase is likely to work.

## How to Get Started

`GodRunner` is the entry point.  
Here, you specify the benchmark you wish to run and the number of iterations.  
If you wish, you can set `lowFootPrint`. In this mode, we try to clean up after ourselves by deleting all VTune runs once they are completed. This is recommended for longer runs and on machines with limited disk space.

## Notes About GT Slowdown Scheduler

Currently, this only supports AWFY benchmarks. Additionally, it is designed to work on Intel CPUs as we utilize VTune hardware counters.
