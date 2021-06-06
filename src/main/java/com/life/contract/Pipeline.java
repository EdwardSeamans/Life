package com.life.contract;

import javafx.beans.property.StringProperty;

public interface Pipeline {
    Runnable getTask();
    StringProperty actionNameProperty();
}
