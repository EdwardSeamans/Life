package com.life;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class LifeFx implements ApplicationListener<StageReadyEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(LifeFx.class);

    private static final int COLUMNS = GlobalSettings.COLUMNS;
    private static final int ROWS = GlobalSettings.ROWS;
    private static final int BYTES_PER_PIXEL = GlobalSettings.BYTES_PER_PIXEL;

    private final FrameQueue frameQueue;

    private PixelWriter pixelWriter;
    private static final PixelFormat<ByteBuffer> pixelFormat = PixelFormat.getByteRgbInstance();

    public LifeFx(FrameQueue frameQueue) {
        this.frameQueue = frameQueue;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent stageReadyEvent) {
        LOG.info("LifeFx Thread:" + Thread.currentThread().getName());
        Stage stage = stageReadyEvent.getStage();

        WritableImage writableImage = new WritableImage(COLUMNS * 2, ROWS * 2);
        this.pixelWriter = writableImage.getPixelWriter();
        ImageView imageView = new ImageView(writableImage);

        HBox root = new HBox(imageView);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: black");

        Scene scene = new Scene(root, COLUMNS * 2, ROWS * 2);
        stage.setScene(scene);
        stage.setTitle("Life");
        stage.setResizable(false);
        stage.show();
    }

    @Scheduled(fixedRate = GlobalSettings.TARGET_FRAME_INTERVAL, initialDelay = 1000)
    public void renderFrame() {
        pixelWriter.setPixels(0, 0, COLUMNS * 2, ROWS * 2, pixelFormat, frameQueue.getNextFrame(), 0, COLUMNS * BYTES_PER_PIXEL * 2);
    }
}