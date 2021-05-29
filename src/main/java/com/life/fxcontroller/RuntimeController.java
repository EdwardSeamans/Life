package com.life.fxcontroller;

import com.life.event.RenderingStageReadyEvent;
import com.life.event.RuntimeStageReadyEvent;
import com.life.event.StartRunEvent;
import com.life.event.StopRunEvent;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
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
    private ColorPicker colorPicker;

    ConfigurationController configurationController;

    private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public RuntimeController(ApplicationContext context, ConfigurationController configurationController) {
        this.context = context;
        this.configurationController = configurationController;
        this.startButton = new Button("Start");
        this.stopButton = new Button("Stop");
        this.colorPicker = new ColorPicker();
    }

    @EventListener
    public void initialize(RuntimeStageReadyEvent event) {
        stage = event.getStage();
        context.publishEvent(new RenderingStageReadyEvent(new Stage()));
        startButton.setOnAction(actionEvent -> go(actionEvent));
        stopButton.setOnAction(actionEvent -> stop(actionEvent));
        colorPicker.getStyleClass().add("button");
        colorPicker.setMaxWidth(29.0);
        HBox hBox = new HBox();
        hBox.setMinSize(200.0, 200.0);
        hBox.getChildren().add(startButton);
        hBox.getChildren().add(stopButton);
        hBox.getChildren().add(colorPicker);
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

    public ObjectProperty<Color> colorProperty() {
        return colorPicker.valueProperty();
    }
}

//target frame rate
//frame rate
//dropped generations
//total generations

