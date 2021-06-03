package com.life.render;

public class Frame {
    public final byte[] buffer;
    public final long frameIndex;

    public Frame(byte[] buffer, long frameIndex) {
        this.buffer = buffer;
        this.frameIndex = frameIndex;
    }
}
