package com.life.pipeline;

import com.life.configuration.IterationSettings;
import com.life.contract.Pipeline;
import com.life.event.RenderingStageReadyEvent;
import com.life.executor.PipelineExecutor;
import com.life.payload.Frame;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;

import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Controller
public class RenderingControllerQueue extends SynchronousQueue<Frame> implements Pipeline {

    private final PipelineExecutor pipelineExecutor;

    private final Runnable action;
    private final AtomicBoolean stop;
    private final StringProperty actionNameProperty;

    private final ImageView imageView;
    private final PixelWriter pixelWriter;
    private static final PixelFormat<ByteBuffer> pixelFormat = PixelFormat.getByteRgbInstance();

    private static final int COLUMNS = IterationSettings.COLUMNS;
    private static final int ROWS = IterationSettings.ROWS;
    private static final int BYTES_PER_PIXEL = IterationSettings.BYTES_PER_PIXEL;
    private static final int SCALING_FACTOR = IterationSettings.SCALING_FACTOR;

    private static final String ACTION_NAME_PROPERTY_STRING = "Render Frames";
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public RenderingControllerQueue(PipelineExecutor pipelineExecutor) {
        super(true);
        this.action = this::renderFrames;
        this.actionNameProperty = new SimpleStringProperty(ACTION_NAME_PROPERTY_STRING);
        this.pipelineExecutor = pipelineExecutor;
        this.stop = pipelineExecutor.getStop();
        WritableImage writableImage = new WritableImage(COLUMNS * SCALING_FACTOR, ROWS * SCALING_FACTOR);
        this.pixelWriter = writableImage.getPixelWriter();
        this.imageView = new ImageView(writableImage);
    }

    @EventListener
    public void initialize(RenderingStageReadyEvent renderingStageReadyEvent) {
        LOG.info("LifeFx Thread:" + Thread.currentThread().getName());
        AnchorPane root = new AnchorPane(imageView);
        Scene scene = new Scene(root, COLUMNS * SCALING_FACTOR, ROWS * SCALING_FACTOR);
        Stage stage = renderingStageReadyEvent.getStage();
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        pipelineExecutor.registerAndRun(this);
    }

    public void renderFrames() {
        while (!stop.get()) {
            try {
                pixelWriter.setPixels(0, 0, COLUMNS * SCALING_FACTOR, ROWS * SCALING_FACTOR,
                        pixelFormat, take().buffer, 0, COLUMNS * BYTES_PER_PIXEL * SCALING_FACTOR);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Runnable getTask() {
        return this.action;
    }

    @Override
    public StringProperty actionNameProperty() {
        return actionNameProperty;
    }

    public ImageView getImageView() {
        return  imageView;
    }
}