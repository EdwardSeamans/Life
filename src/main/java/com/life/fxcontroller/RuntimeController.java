package com.life.fxcontroller;

import com.life.color.RgbConvertedColor;
import com.life.configuration.IterationSettings;
import com.life.event.RenderingStageReadyEvent;
import com.life.event.RuntimeStageReadyEvent;
import com.life.event.StartRunEvent;
import com.life.event.StopRunEvent;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

@Component
public class RuntimeController {

    private final ApplicationContext context;
    private Stage stage;
    private Button startButton;
    private Button stopButton;
    private ColorPicker liveColorPicker;
    private ColorPicker deadColorPicker;

    private final RgbConvertedColor deadCellRgbConvertedColor;
    private final RgbConvertedColor liveCellRgbConvertedColor;

    ConfigurationController configurationController;

    private static final int COLUMNS = IterationSettings.COLUMNS;
    private static final int ROWS = IterationSettings.ROWS;
    private static final int INITIAL_POPULATION_PERCENT = IterationSettings.INITIAL_POPULATION_PERCENT;
    private static final int[] BIRTH = IterationSettings.BIRTH;
    private static final int[] SURVIVE = IterationSettings.SURVIVE;

    private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public RuntimeController(ApplicationContext context, ConfigurationController configurationController) {
        this.context = context;
        this.configurationController = configurationController;
        this.startButton = new Button("Start");
        this.stopButton = new Button("Stop");
        this.liveColorPicker = new ColorPicker(Color.BLACK);
        this.liveCellRgbConvertedColor = new RgbConvertedColor(liveColorPicker.getValue());
        this.deadColorPicker = new ColorPicker(Color.WHITE);
        this.deadCellRgbConvertedColor = new RgbConvertedColor(deadColorPicker.getValue());
    }

    @EventListener
    public void initialize(RuntimeStageReadyEvent event) {
        stage = event.getStage();
        context.publishEvent(new RenderingStageReadyEvent(new Stage(StageStyle.UNDECORATED)));
        startButton.setOnAction(this::go);
        stopButton.setOnAction(this::stop);
        liveColorPicker.getStyleClass().add("button");
        liveColorPicker.setMaxWidth(29.0);
        liveColorPicker.valueProperty().addListener((observableValue, oldValue, newValue) -> liveCellRgbConvertedColor.update(newValue));
        deadColorPicker.getStyleClass().add("button");
        deadColorPicker.setMaxWidth(29.0);
        deadColorPicker.valueProperty().addListener((observableValue, oldValue, newValue) -> deadCellRgbConvertedColor.update(newValue));
        HBox hBox = new HBox();
        hBox.setMinSize(200.0, 200.0);
        hBox.getChildren().add(startButton);
        hBox.getChildren().add(stopButton);
        hBox.getChildren().add(liveColorPicker);
        hBox.getChildren().add(deadColorPicker);
        Scene scene = new Scene(hBox);
        stage.setScene(scene);
        stage.show();
    }

    private void go(ActionEvent event) {
        context.publishEvent(new StartRunEvent());
    }

    private void stop(ActionEvent event) {
        context.publishEvent(new StopRunEvent());
    }

    public RgbConvertedColor getDeadCellRgbConvertedColor() {
        return deadCellRgbConvertedColor;
    }

    public RgbConvertedColor getLiveCellRgbConvertedColor() {
        return liveCellRgbConvertedColor;
    }
}

//target frame rate
//frame rate
//dropped generations
//total generations

