package com.github.rapgru.propertycluster;

import javafx.beans.property.Property;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class PropertyCluster {

    private HashMap<String, PropertyClusterProperty<?>> properties = new HashMap<>();
    private AtomicBoolean clusterUp = new AtomicBoolean(true);

    public static PropertyClusterRegisterPhase build() {
        return new PropertyClusterRegisterPhase();
    }

    AtomicBoolean getClusterUp(){
        return clusterUp;
    }

    PropertyCluster () {}

    public HashMap<String, PropertyClusterProperty<?>> getProperties() {
        return properties;
    }
    public Property<?> p(String name){
        return properties.get(name).pseudoProperty();
    }

    public void stopCluster(){
        clusterUp.set(false);
        properties.forEach((name, prop) -> prop.getPseudoQueue().interruptWorker());
    }
}
