package com.github.rapgru.propertycluster;

import javafx.beans.property.Property;

class PropertyClusterPropertyChange<T> {

    private T newValue;
    private Property<T> source;
    private boolean updateOverQueue;

    PropertyClusterPropertyChange(T newValue, Property<T> source, boolean updateOverQueue) {
        this.newValue = newValue;
        this.source = source;
        this.updateOverQueue = updateOverQueue;
    }

    T getNewValue() {
        return newValue;
    }

    boolean isUpdateOverQueue() {
        return updateOverQueue;
    }

    Property<T> getSource() {
        return source;
    }

    Property<T> sourceProperty() {
        return source;
    }
}
