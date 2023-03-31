package com.life.exception;

import com.life.executor.PipelineExecutor;
import com.life.fxcontroller.RuntimeController;
import com.life.pipeline.RenderingControllerQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.invoke.MethodHandles;

@Component
public class FxUncaughtExceptionHandler implements UncaughtExceptionHandler {

    private final ConfigurableApplicationContext context;
    private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    FxUncaughtExceptionHandler(ConfigurableApplicationContext context) {
        this.context = context;
    }
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        PipelineExecutor pipelineExecutor = context.getBean(PipelineExecutor.class);
        pipelineExecutor.isPaused().set(true);
        context.getBean(RenderingControllerQueue.class).recoverFromQuantumToolkitError(context.getBean(RuntimeController.class).getStage());
        pipelineExecutor.isPaused().set(false);
        LOG.error("FX Encountered an internal exception!");
        LOG.error(e.getMessage());
        LOG.info("Recovered from internal exception.");
    }
}
