package com.life;


import com.life.fxplatform.LifeFXApplication;
import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.lang.invoke.MethodHandles;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class LifeSpringBootApplication {

    private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {
        LOG.info("Invoking FX Application in main() method.");
        Application.launch(LifeFXApplication.class, args);
    }
}
