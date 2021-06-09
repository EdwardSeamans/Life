package com.life.color;

import javafx.scene.paint.Color;

public class RgbConvertedColor {

    private final byte[] byteValueArray;
    public byte[] updatedByteValueArray;

    public boolean valueChanged;

    public RgbConvertedColor(Color color) {
        byteValueArray = new byte[3];
        update(color);
    }

    public void update(Color color) {
        byteValueArray[0] = (byte)(Math.floor(color.getRed() * 255));
        byteValueArray[1] = (byte)(Math.floor(color.getGreen() * 255));
        byteValueArray[2] = (byte)(Math.floor(color.getBlue() * 255));

        updatedByteValueArray = new byte[3];

        System.arraycopy(byteValueArray, 0, updatedByteValueArray, 0, 3);
        valueChanged = true;
    }

    public byte[] getColorChunk() {
        valueChanged = false;
        return updatedByteValueArray;
    }
}