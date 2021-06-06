package com.life.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

@Component
public class AsynchronousLogger {
    private final Logger log;

    AsynchronousLogger() {
        this.log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    }

    @Async
    public void info(String message) {
        log.info(message);
    }
}
