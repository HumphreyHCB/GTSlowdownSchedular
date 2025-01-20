package Phases.Divining;

import Phases.Divining.MarkerPhaseDataLookup.BlockInfo;

/**
 * This class tracks slowdown attempts and times for a single block.
 * It allows you to record a new measurement (slowdown + CPU time) each iteration,
 * and if we detect that we crossed from under-target to over-target, it decides
 * which slowdown value is closer to the target time.
 */
public class BlockSlowdownTracker {

    private final BlockInfo blockInfo;
    /**
     * The target CPU time we want for this block.
     * e.g. blockInfo.baseCpuTime * slowdownAmount
     */
    private final double targetTime;

    // Last known slowdown/time (from the previous iteration)
    private Integer lastSlowdown;
    private Double lastMeasuredTime;

    // Current slowdown/time (from the current iteration)
    private Integer currentSlowdown;
    private Double currentMeasuredTime;

    // Once we finalize the slowdown, we store it here.
    private Integer finalSlowdown = null;

    // Whether this block is finished (i.e., we decided on a slowdown).
    public boolean isDone = false;

    /**
     * Constructor
     *
     * @param blockInfo      the block we're trying to slow down
     * @param slowdownAmount the factor by which we want to slow it, e.g. 2.0 for 2x
     */
    public BlockSlowdownTracker(BlockInfo blockInfo, double slowdownAmount) {
        this.blockInfo = blockInfo;
        this.targetTime = blockInfo.baseCpuTime * slowdownAmount;
    }

    /**
     * Record a new measurement (slowdown value + measured CPU time) for this iteration.
     * This shifts the current iteration's data to "last" and sets the new data to "current."
     *
     * @param slowdownValue   The slowdown level we used this iteration
     * @param measuredCpuTime The CPU time that VTune or some tool measured for this block
     */
    public void recordMeasurement(int slowdownValue, double measuredCpuTime) {
        // Shift the current iteration to 'last'
        if (currentSlowdown != null) {
            lastSlowdown = currentSlowdown;
            lastMeasuredTime = currentMeasuredTime;
        }

        // Set the new current iteration data
        currentSlowdown = slowdownValue;
        currentMeasuredTime = measuredCpuTime;
    }

    /**
     * Check if the current measurement is under the target time or not.
     *
     * @return true if currentMeasuredTime < targetTime, false otherwise
     */
    public boolean isUnderTarget() {
        if (currentMeasuredTime == null) {
            return false;
        }
        return currentMeasuredTime < targetTime;
    }

    /**
     * Check if the current measurement is over the target time or not.
     *
     * @return true if currentMeasuredTime > targetTime, false otherwise
     */
    public boolean isOverTarget() {
        if (currentMeasuredTime == null) {
            return false;
        }
        return currentMeasuredTime > targetTime;
    }

    /**
     * Attempt to finalize the slowdown if we've overshot for the first time,
     * or if we otherwise know we're done. The idea is:
     * 1. If we have no "previous" measurement or both are over the target, just pick current.
     * 2. If we jumped from under-target to over-target, compare which one is closer to target.
     *    Whichever is closer is our final slowdown.
     *
     * @return the final slowdown if determined here, or null if not yet finalized.
     */
    public Integer determineBestSlowdownIfOvershot() {
        // If we already have a final slowdown or no current measurement, do nothing
        if (isDone || currentMeasuredTime == null) {
            return null;
        }

        // If we're not over the target, we can't finalize yet
        if (!isOverTarget()) {
            return null;
        }

        // If there's no "last" measurement, or the last measurement was also over target,
        // we can just pick the current slowdown as final
        if (lastMeasuredTime == null || lastMeasuredTime > targetTime) {
            finalSlowdown = currentSlowdown;
            isDone = true;
            return finalSlowdown;
        }

        // Otherwise, we jumped from "under target" to "over target".
        // Check which iteration's time is closer to target.
        double diffLast = Math.abs(lastMeasuredTime - targetTime);
        double diffCurrent = Math.abs(currentMeasuredTime - targetTime);

        if (diffLast <= diffCurrent) {
            // Previous iteration was closer
            finalSlowdown = lastSlowdown;
        } else {
            // Current iteration is closer
            finalSlowdown = currentSlowdown;
        }

        isDone = true;
        return finalSlowdown;
    }

    public Integer getFinalSlowdown() {
        return finalSlowdown;
    }

    public boolean isDone() {
        return isDone;
    }

    public BlockInfo getBlockInfo() {
        return blockInfo;
    }

    public double getTargetTime() {
        return targetTime;
    }

    public Integer getLastSlowdown() {
        return lastSlowdown;
    }

    public Double getLastMeasuredTime() {
        return lastMeasuredTime;
    }

    public Integer getCurrentSlowdown() {
        return currentSlowdown;
    }

    public Double getCurrentMeasuredTime() {
        return currentMeasuredTime;
    }
}
