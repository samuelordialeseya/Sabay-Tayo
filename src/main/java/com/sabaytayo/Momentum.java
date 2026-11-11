package com.sabaytayo;

// Tracks conversation flow from -5 to +5 and scales positive stat gains.
public class Momentum {

    private int value;

    // Starts at 0
    public Momentum() {
        this.value = 0;
    }

    // Clamps momentum value between -5 and +5
    public void adjust(int delta) {
        this.value = Math.max(-5, Math.min(5, this.value + delta));
    }

    public int getValue() {
        return this.value;
    }

    // Returns 1.5 if momentum >= 3, 0.5 if momentum <= -3, else 1.0
    public double getMultiplier() {
        if (this.value >= 3) {
            return 1.5;
        } else if (this.value <= -3) {
            return 0.5;
        }
        return 1.0;
    }

    // Scales positive stat gains by the current multiplier (e.g. +8 becomes +12 or +4)
    public int scaleGain(int baseGain) {
        if (baseGain <= 0) {
            return baseGain;
        }
        return (int) Math.round(baseGain * getMultiplier());
    }
}
