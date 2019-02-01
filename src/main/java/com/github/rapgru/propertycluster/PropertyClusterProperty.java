package com.github.rapgru.propertycluster;

import com.github.rapgru.propertycluster.configuration.PropertyClusterConfigException;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents a cluster property
 * @param <T>
 */
public class PropertyClusterProperty<T> {

    private String name;
    //private T value;
    private AtomicReference<T> value;
    private ArrayList<PropertyClusterRelation<T, ?>> relations = new ArrayList<>();
    private Vector<Property<T>> pseudoProperties = new Vector<>();
    private PropertyClusterQueue<T> pseudoQueue = new PropertyClusterQueue<>(this);
    private PropertyCluster cluster;
    private Class valueClass;

    /**
     * Represents an pseudo property as object and adds the functionality of storing, activating and deactivating an change listener
     *
     * @param <PT> the pseudo property's type
     */
    public class PropertyClusterPseudoProperty<PT> extends SimpleObjectProperty<PT>
    {
        ChangeListener<PT> clusterListener;

        PropertyClusterPseudoProperty(PT initialValue) {
            super(initialValue);
        }

        void setClusterListener(ChangeListener<PT> listener){
            clusterListener = listener;
        }

        void activateClusterListener()
        {
            this.addListener(clusterListener);
        }

        void deactivateClusterListener(){
            this.removeListener(clusterListener);
        }

    }

    /**
     * Construct a new cluster property
     * @param name name of the cluster property
     * @param value inital value of the cluster property
     * @param cluster the cluster this cluster property belongs to
     *
     * @see com.github.rapgru.propertycluster.configuration.PropertyClusterRegisterPhase#add(String, Object) corresponding configuration method
     */
    public PropertyClusterProperty(String name, T value, PropertyCluster cluster) {
        this.name = name;
        this.cluster = cluster;
        this.value = new AtomicReference<>(value);
        this.valueClass = value.getClass();
    }

    /**
     * Gets the name of a cluster property
     * @return the name
     */
    public String getName() {
        return name;
    }


    Property<T> pseudoProperty() {
        PropertyClusterPseudoProperty<T> prop = new PropertyClusterPseudoProperty<>(value.get());

        pseudoProperties.add(prop);
        prop.setClusterListener((observable, oldValue, newValue) -> pseudoQueue.update(new PropertyClusterPropertyChange<>(newValue, prop,true)));
        prop.activateClusterListener();
        return prop;
    }


    void set(T value){
        pseudoQueue.update(new PropertyClusterPropertyChange<>(value, null, false));

        System.out.println("normal set");

        pseudoProperties.forEach(pseudoProp ->
            Platform.runLater(() -> {
                ((PropertyClusterPseudoProperty<T>)pseudoProp).deactivateClusterListener();
                pseudoProp.setValue(value);
                ((PropertyClusterPseudoProperty<T>)pseudoProp).activateClusterListener();
            })
        );

        relations.stream().sorted(Comparator.comparingInt(PropertyClusterRelation::getPriority)).forEach(rel -> rel.apply(value));
    }

    void setByRelation(T value, PropertyClusterRelation<?, T> by){
        pseudoQueue.update(new PropertyClusterPropertyChange<>(value, null, false));

        System.out.println("set by relation");

        pseudoProperties.forEach(pseudoProp ->
            Platform.runLater(() -> {
                ((PropertyClusterPseudoProperty<T>)pseudoProp).deactivateClusterListener();
                pseudoProp.setValue(value);
                ((PropertyClusterPseudoProperty<T>)pseudoProp).activateClusterListener();
            })
        );

        relations.stream()
                .filter(rel -> rel.getTo() != by.getFrom())
                .filter(rel -> rel.getPrev().test(by.getFrom()))
                .sorted(Comparator.comparingInt(PropertyClusterRelation::getPriority))
                .forEach(rel -> rel.apply(value));
    }

    /**
     * Add a relation to a cluster property
     * Note: Relations are only stored at their from side!
     *
     * @param rel the relation to add
     */
    public void addRelation(PropertyClusterRelation<T, ?> rel) throws PropertyClusterConfigException
    {
        if(rel.getFrom() != this) throw new PropertyClusterConfigException();
        relations.add(rel);
    }

    AtomicReference<T> getValue(){
        return value;
    }

    PropertyCluster getCluster() {
        return cluster;
    }

    PropertyClusterQueue<T> getPseudoQueue() {
        return pseudoQueue;
    }
}
