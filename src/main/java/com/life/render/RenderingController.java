package com.life.render;

import com.life.configuration.IterationSettings;
import com.life.event.RenderingStageReadyEvent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;

@Controller
public class RenderingController {

    private static final int COLUMNS = IterationSettings.COLUMNS;
    private static final int ROWS = IterationSettings.ROWS;
    private static final int BYTES_PER_PIXEL = IterationSettings.BYTES_PER_PIXEL;
    private static final int SCALING_FACTOR = IterationSettings.SCALING_FACTOR;

    private final FrameQueue frameQueue;

    private PixelWriter pixelWriter;
    private static final PixelFormat<ByteBuffer> pixelFormat = PixelFormat.getByteRgbInstance();

    private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public RenderingController(FrameQueue frameQueue) {
        this.frameQueue = frameQueue;
    }

    @EventListener
    public void initialize(RenderingStageReadyEvent renderingStageReadyEvent) {
        LOG.info("LifeFx Thread:" + Thread.currentThread().getName());
        Stage stage = renderingStageReadyEvent.getStage();

        WritableImage writableImage = new WritableImage(COLUMNS * SCALING_FACTOR, ROWS * SCALING_FACTOR);
        this.pixelWriter = writableImage.getPixelWriter();
        ImageView imageView = new ImageView(writableImage);

        AnchorPane root = new AnchorPane(imageView);

        Scene scene = new Scene(root, COLUMNS * SCALING_FACTOR, ROWS * SCALING_FACTOR);
        stage.setScene(scene);
        stage.setTitle("Life");
        stage.setResizable(false);
        stage.show();
    }

    @Scheduled(fixedRate = 5, initialDelay = 10000)
    public void renderFrame() {
        pixelWriter.setPixels(0, 0, COLUMNS * SCALING_FACTOR, ROWS * SCALING_FACTOR,
                pixelFormat, frameQueue.getNextFrame(), 0, COLUMNS * BYTES_PER_PIXEL * SCALING_FACTOR);
    }
}