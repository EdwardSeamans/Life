package com.life.executor;

import com.life.contract.Pipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class PipelineExecutor extends ThreadPoolExecutor {

    private final AtomicBoolean stop;

    private static final int WORKERS = 4;
    private static final long IMMORTAL = Long.MAX_VALUE;

    private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public PipelineExecutor() {
        super(4, WORKERS, IMMORTAL, TimeUnit.DAYS, new ArrayBlockingQueue<>(WORKERS, true));
        this.stop = new AtomicBoolean();
        prestartAllCoreThreads();
        LOG.info("PipelineExecutor started. Available workers: " + getPoolSize());
    }

    public AtomicBoolean getStop() {
        return stop;
    }

    public void registerAndRun(Pipeline pipelineComponent) {
        LOG.info("Starting task: " + pipelineComponent.actionNameProperty().get());
        execute(pipelineComponent.getTask());
    }
}
