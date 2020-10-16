package com.life;


import com.life.fx.LifeFXApplication;
import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LifeSpringBootApplication {

    private static final Logger LOG = LoggerFactory.getLogger(LifeSpringBootApplication.class);

    public static void main(String[] args) {
        LOG.info("Invoking FX Application in main() method.");
        Application.launch(LifeFXApplication.class, args);
    }
}
