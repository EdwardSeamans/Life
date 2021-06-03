package com.life.render;

import com.life.configuration.IterationSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class FrameQueue {
    private final ConcurrentLinkedQueue<Frame> frameQueue;

    private final FrameTimer frameTimer;

    int framesInBuffer = 50;

    private int columns = IterationSettings.COLUMNS;
    private int rows = IterationSettings.ROWS;
    private int bytesPerPixel = IterationSettings.BYTES_PER_PIXEL;

    private byte[] currentFrame;

    private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public FrameQueue(FrameTimer frameTimer) {
        LOG.info("FrameQueue construction thread is " + Thread.currentThread().getName());
        this.frameTimer = frameTimer;
        this.frameQueue = new ConcurrentLinkedQueue<>();
        currentFrame = new byte[IterationSettings.SCALING_FACTOR * columns * IterationSettings.SCALING_FACTOR * rows * bytesPerPixel];
    }

    public byte[] getNextFrame() {
        if (frameQueue.isEmpty()) {
            frameTimer.tock();
            return currentFrame;
        }
        frameTimer.tick();
        currentFrame = frameQueue.remove().buffer;
        return currentFrame;
    }

    public void publish(Frame frame) {
        frameQueue.add(frame);
    }

    public boolean needsFrame() {
        return frameQueue.size() < framesInBuffer;
    }
}
