package com.life.contract;

import java.util.concurrent.atomic.AtomicBoolean;

public interface Stoppable {
    void setStop(AtomicBoolean stop);
}
