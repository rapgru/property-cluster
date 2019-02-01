package com.github.rapgru.propertycluster.configuration;

import com.github.rapgru.propertycluster.PropertyCluster;
import com.github.rapgru.propertycluster.PropertyClusterProperty;

/**
 * Class for registering new cluster properties to a cluster
 */
public class PropertyClusterRegisterPhase {

    private PropertyCluster cluster = new PropertyCluster();

    /**
     * Adds a cluster property to the configuration
     *
     * @param name name of the new cluster property
     * @param initVal the initial value of this cluster property
     *                the cluster property's type is derived from this value
     * @param <T> type of initial value, cluster property is created from this type
     * @return the same {@link PropertyClusterRegisterPhase} object for further configuration
     */
    public <T> PropertyClusterRegisterPhase add(String name, T initVal){
        cluster.getProperties().put(name, new PropertyClusterProperty<>(name, initVal, cluster));
        return this;
    }

    /**
     * Continues to the next configuration phase, where you can specify relations
     * @return object for configuring relations
     */
    public PropertyClusterRelationPhase next(){
        return new PropertyClusterRelationPhase(cluster);
    }
}
