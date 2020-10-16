package com.life.render;

import com.life.configuration.IterationSettings;
import com.life.utility.StageReadyEvent;
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
public class RenderingController implements ApplicationListener<StageReadyEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(RenderingController.class);

    private static final int COLUMNS = IterationSettings.COLUMNS;
    private static final int ROWS = IterationSettings.ROWS;
    private static final int BYTES_PER_PIXEL = IterationSettings.BYTES_PER_PIXEL;
    private static final int SCALING_FACTOR = IterationSettings.SCALING_FACTOR;
    private static final long TARGET_FRAME_INTERVAL = IterationSettings.TARGET_FRAME_INTERVAL;

    private final FrameQueue frameQueue;

    private PixelWriter pixelWriter;
    private static final PixelFormat<ByteBuffer> pixelFormat = PixelFormat.getByteRgbInstance();

    public RenderingController(FrameQueue frameQueue) {
        this.frameQueue = frameQueue;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent stageReadyEvent) {
        LOG.info("LifeFx Thread:" + Thread.currentThread().getName());
        Stage stage = stageReadyEvent.getStage();

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

    @Scheduled(fixedRate = TARGET_FRAME_INTERVAL, initialDelay = 1000)
    public void renderFrame() {
        pixelWriter.setPixels(0, 0, COLUMNS * SCALING_FACTOR, ROWS * SCALING_FACTOR,
                pixelFormat, frameQueue.getNextFrame(), 0, COLUMNS * BYTES_PER_PIXEL * SCALING_FACTOR);
    }
}