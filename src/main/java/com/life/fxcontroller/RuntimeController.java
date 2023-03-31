package com.life.fxcontroller;

import com.life.color.RgbConvertedColor;
import com.life.event.RenderingStageReadyEvent;
import com.life.event.RuntimeStageReadyEvent;
import com.life.executor.PipelineExecutor;
import com.life.pipeline.FrameProducingQueue;
import com.life.pipeline.GenerationProcessingQueue;
import com.life.utility.FrameTimer;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class RuntimeController {

    private final Button pauseButton;
    private final ColorPicker liveColorPicker;
    private final ColorPicker deadColorPicker;

    private final Label framesPerSecondLabel;
    private final Label cycleInformationLabel;
    private final RgbConvertedColor deadCellRgbConvertedColor;
    private final RgbConvertedColor liveCellRgbConvertedColor;
    private final FrameTimer frameTimer;
    private final GenerationProcessingQueue generationProcessingQueue;
    private final ConfigurableApplicationContext context;

    private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public RuntimeController(ConfigurableApplicationContext context, FrameTimer frameTimer, GenerationProcessingQueue generationProcessingQueue,
                             FrameProducingQueue frameProducingQueue) {
        this.context = context;
        this.frameTimer = frameTimer;
        this.generationProcessingQueue = generationProcessingQueue;
        this.pauseButton = new Button("⏵");
        this.liveColorPicker = new ColorPicker(Color.LIMEGREEN);
        this.liveCellRgbConvertedColor = new RgbConvertedColor(liveColorPicker.getValue());
        this.deadColorPicker = new ColorPicker(Color.BLACK);
        this.deadCellRgbConvertedColor = new RgbConvertedColor(deadColorPicker.getValue());
        this.framesPerSecondLabel = new Label();
        this.cycleInformationLabel = new Label();
        frameProducingQueue.initializeAndLaunch(deadCellRgbConvertedColor, liveCellRgbConvertedColor);
    }

    @EventListener
    public void initialize(RuntimeStageReadyEvent event) {
        Stage stage = event.getStage();
        Thread.currentThread().setUncaughtExceptionHandler(event.getFxUncaughtExceptionHandler());
        stage.setTitle("Life");
        File file;
        try {
            file = ResourceUtils.getFile("classpath:Glider.gif");
            Image image = new Image(file.toURI().toString());
            stage.getIcons().add(image);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        context.publishEvent(new RenderingStageReadyEvent(new Stage(StageStyle.UNDECORATED)));
        pauseButton.setOnAction(this::pause);
        liveColorPicker.getStyleClass().add("button");
        liveColorPicker.setMaxWidth(29.0);
        liveColorPicker.valueProperty().addListener((observableValue, oldValue, newValue) -> liveCellRgbConvertedColor.update(newValue));
        deadColorPicker.getStyleClass().add("button");
        deadColorPicker.setMaxWidth(29.0);
        deadColorPicker.valueProperty().addListener((observableValue, oldValue, newValue) -> deadCellRgbConvertedColor.update(newValue));
        this.framesPerSecondLabel.textProperty().bind(frameTimer.framesPerSecondProperty());
        this.cycleInformationLabel.textProperty().bind(generationProcessingQueue.cycleInformationProperty());
        FlowPane flowPane = new FlowPane();
        flowPane.getChildren().add(pauseButton);
        flowPane.getChildren().add(liveColorPicker);
        flowPane.getChildren().add(deadColorPicker);
        flowPane.getChildren().add(cycleInformationLabel);
        flowPane.getChildren().add(framesPerSecondLabel);
        Scene scene = new Scene(flowPane);
        stage.setScene(scene);
        stage.show();
    }

    private void pause(ActionEvent event) {
        AtomicBoolean isPaused = context.getBean(PipelineExecutor.class).isPaused();
        isPaused.set(!isPaused.get());
        if (isPaused.get()) {
            pauseButton.textProperty().set("⏵");
        } else {
            pauseButton.textProperty().set("⏸");
        }
    }
}

