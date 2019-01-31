package com.github.rapgru.propertycluster;


import javafx.beans.property.Property;

public class PropertyClusterPropertyChange<T>{

    private T newValue;
    private Property<T> source;
    private boolean updateOverQueue;

    public PropertyClusterPropertyChange(T newValue, Property<T> source, boolean updateOverQueue) {
        this.newValue = newValue;
        this.source = source;
        this.updateOverQueue = updateOverQueue;
    }

    public T getNewValue() {
        return newValue;
    }

    public boolean isUpdateOverQueue() {
        return updateOverQueue;
    }

    public Property<T> getSource() {
        return source;
    }

    public Property<T> sourceProperty() {
        return source;
    }
}
