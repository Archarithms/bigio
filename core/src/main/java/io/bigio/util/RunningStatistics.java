/*
 * Copyright (c) 2015, Archarithms Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the FreeBSD Project.
 */

package io.bigio.util;

import java.io.Serializable;

/**
 * A utility class for keeping track of running statistics.
 * 
 * @author Andy Trimble
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
