/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.bigio.util;

import java.io.Serializable;

/**
 *
 * @author atrimble
 */
public class RunningStatistics implements Serializable {

    private int numSamples = 0;
    private double oldMean;
    private double newMean;
    private double oldStDev;
    private double newStDev;

    public void clear() {
        numSamples = 0;
    }

    public void push(double x) {
        numSamples++;

        // See Knuth TAOCP vol 2, 3rd edition, page 232
        if (numSamples == 1) {
            oldMean = newMean = x;
            oldStDev = 0.0;
        } else {
            newMean = oldMean + (x - oldMean) / numSamples;
            newStDev = oldStDev + (x - oldMean) * (x - newMean);

            // set up for next iteration
            oldMean = newMean;
            oldStDev = newStDev;
        }
    }

    public int numSamples() {
        return numSamples;
    }

    public double mean() {
        return (numSamples > 0) ? newMean : 0.0;
    }

    public double variance() {
        return ((numSamples > 1) ? newStDev / (numSamples - 1) : 0.0);
    }

    public double standardDeviation() {
        return Math.sqrt(variance());
    }
}
