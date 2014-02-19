/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.speak.core.codec;

import com.a2i.sim.core.Message;

/**
 *
 * @author atrimble
 */
@Message
public class ECEF {
    private double x;
    private double y;
    private double z;

    public ECEF(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public byte[] test() {
        return new byte[0];
    }

    /**
     * @return the x
     */
    public double getX() {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public double getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * @return the z
     */
    public double getZ() {
        return z;
    }

    /**
     * @param z the z to set
     */
    public void setZ(double z) {
        this.z = z;
    }
}
