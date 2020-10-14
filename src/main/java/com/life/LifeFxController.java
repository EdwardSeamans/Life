package com.life;

import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.nio.ByteBuffer;

@Controller
public class LifeFxController implements ApplicationListener<StageReadyEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(LifeFxController.class);

    private static final int COLUMNS = IterationSettings.COLUMNS;
    private static final int ROWS = IterationSettings.ROWS;
    private static final int BYTES_PER_PIXEL = IterationSettings.BYTES_PER_PIXEL;

    private final FrameQueue frameQueue;

    private PixelWriter pixelWriter;
    private static final PixelFormat<ByteBuffer> pixelFormat = PixelFormat.getByteRgbInstance();

    public LifeFxController(FrameQueue frameQueue) {
        this.frameQueue = frameQueue;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent stageReadyEvent) {
        LOG.info("LifeFx Thread:" + Thread.currentThread().getName());
        Stage stage = stageReadyEvent.getStage();

        WritableImage writableImage = new WritableImage(COLUMNS * IterationSettings.SCALING_FACTOR, ROWS * IterationSettings.SCALING_FACTOR);
        this.pixelWriter = writableImage.getPixelWriter();
        ImageView imageView = new ImageView(writableImage);

        AnchorPane root = new AnchorPane(imageView);

        Scene scene = new Scene(root, COLUMNS * IterationSettings.SCALING_FACTOR, ROWS * IterationSettings.SCALING_FACTOR);
        stage.setScene(scene);
        stage.setTitle("Life");
        stage.setResizable(false);
        stage.show();
    }

    @Scheduled(fixedRate = IterationSettings.TARGET_FRAME_INTERVAL, initialDelay = 1000)
    public void renderFrame() {
        pixelWriter.setPixels(0, 0, COLUMNS * IterationSettings.SCALING_FACTOR, ROWS * IterationSettings.SCALING_FACTOR,
                pixelFormat, frameQueue.getNextFrame(), 0, COLUMNS * BYTES_PER_PIXEL * IterationSettings.SCALING_FACTOR);
    }
}