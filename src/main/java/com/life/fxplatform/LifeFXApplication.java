package com.life.fxplatform;

import com.life.LifeSpringBootApplication;
import com.life.event.RuntimeStageReadyEvent;
import com.life.exception.FxUncaughtExceptionHandler;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.invoke.MethodHandles;

public class LifeFXApplication extends Application {

    private ConfigurableApplicationContext context;
    private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public void init() {
        Thread.currentThread().setName("Boot");
        LOG.info("Invoking Boot Application in init() method on " + Thread.currentThread().getName());
        context = new SpringApplicationBuilder(LifeSpringBootApplication.class).run();
    }

    @Override
    public void start(Stage stage) {
        Thread.currentThread().setName("JavaFX-Thread");
        LOG.info("Starting FX Application on " + Thread.currentThread().getName());
        context.publishEvent(new RuntimeStageReadyEvent(stage, context.getBean(FxUncaughtExceptionHandler.class)));
    }

    @Override
    public void stop() {
        context.stop();
        Platform.exit();
    }
}