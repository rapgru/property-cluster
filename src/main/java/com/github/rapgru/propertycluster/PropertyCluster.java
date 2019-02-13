package com.github.rapgru.propertycluster;

import com.github.rapgru.propertycluster.configuration.PropertyClusterRegisterPhase;
import javafx.application.Application;
import javafx.beans.property.Property;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The PropertyCluster object that manages and encapsulates multiple cluster properties.
 */
public class PropertyCluster {

    private HashMap<String, PropertyClusterProperty<?>> properties = new HashMap<>();
    private AtomicBoolean clusterUp = new AtomicBoolean(true);

    /**
     * Starts the configuration of a new cluster. Issue configuration commands afterwards to spedify cluster properties and relations.
     *
     * @return A PropertyClusterRegisterPhase object that allows to register cluster properties
     */
    public static PropertyClusterRegisterPhase build() {
        return new PropertyClusterRegisterPhase();
    }

    AtomicBoolean getClusterUp(){
        return clusterUp;
    }


    /**
     * Construct a new property cluster without config.
     */
    public PropertyCluster () {}

    /**
     * Get all cluster properties
     * @return map of all cluster properties
     */
    public HashMap<String, PropertyClusterProperty<?>> getProperties() {
        return properties;
    }

    /**
     * Gets a ({@link javafx.beans.property.Property standard javafx property}) pseudo property for interacting with a cluster property
     * This pseudo property can be set, get or bound to other javafx properties
     *
     * @param name cluster property name
     * @return a pseudo property, need a type cast to the correct type
     */
    public <T> Property<T> p(String name){
        Property<T> p;
        try {
            p = (Property<T>)properties.get(name).pseudoProperty();
        } catch (ClassCastException ex){
            throw new WrongTypeContextException("Used an pseudo property, but the given or inferred type of the p() method call doesn't match the type of the cluster property");
        }
        return p;
    }

    /**
     * Stops all threads belonging to a property cluster.
     * Use for example in your {@link Application#stop()} implementation
     */
    public void stopCluster(){
        clusterUp.set(false);
        properties.forEach((name, prop) -> prop.getPseudoQueue().interruptWorker());
    }
}
