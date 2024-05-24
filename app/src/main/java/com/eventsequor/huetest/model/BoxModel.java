package com.eventsequor.huetest.model;

public class BoxModel {

    private final int color;
    private int position;
    private int value;

    private String colorValue;

    public BoxModel(int color, int position, int value) {
        this.color = color;
        this.position = position;
        this.colorValue = String.format("#%06X", (0xFFFFFF & color));
        this.value = value;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getColor() {
        return color;
    }

    public int getPosition() {
        return position;
    }

    public int getValue() {
        return value;
    }

    public String getColorValue() {
        return colorValue;
    }

    public void setColorValue(String colorValue) {
        this.colorValue = colorValue;
    }
}
