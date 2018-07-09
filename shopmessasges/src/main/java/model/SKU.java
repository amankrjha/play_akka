package model;

import java.io.Serializable;

/**
 * Created by amajha on 6/14/2018.
 */
public class SKU implements Serializable {

    private String memory;
    private String Color;
    private int units;

    public SKU(String memory, String color, int units) {
        this.memory = memory;
        Color = color;
        this.units = units;
    }

    public SKU() {
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getColor() {
        return Color;
    }

    public void setColor(String color) {
        Color = color;
    }

    public int getUnits() {
        return units;
    }

    public void setUnits(int units) {
        this.units = units;
    }
}
