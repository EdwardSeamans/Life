package com.life.fx;
import com.life.LifeSpringBootApplication;
import com.life.utility.StageReadyEvent;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class LifeFXApplication extends Application {

    private ConfigurableApplicationContext applicationContext;
    private static final Logger LOG = LoggerFactory.getLogger(LifeFXApplication.class);

    @Override
    public void init() {
        LOG.info("Invoking Boot Application in init() method.");
        applicationContext = new SpringApplicationBuilder(LifeSpringBootApplication.class).run();
    }

    @Override
    public void start(Stage stage) {
        LOG.info("Starting FX Application.");
        Thread.currentThread().setName("JavaFX Thread");
        applicationContext.publishEvent(new StageReadyEvent(stage));
    }

    @Override
    public void stop() {
        applicationContext.stop();
        Platform.exit();
    }
}