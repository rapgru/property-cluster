package com.github.rapgru.propertycluster;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;

public class PropertyClusterProperty<T> {

    private String name;
    //private T value;
    private AtomicReference<T> value;
    private ArrayList<PropertyClusterRelation<T, ?>> relations = new ArrayList<>();
    private Vector<Property<T>> pseudoProperties = new Vector<>();
    private PropertyClusterQueue<T> pseudoQueue = new PropertyClusterQueue<>(this);
    private PropertyCluster cluster;
    private Class valueClass;

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

    PropertyClusterProperty(String name, T value, PropertyCluster cluster) {
        this.name = name;
        this.cluster = cluster;
        this.value = new AtomicReference<T>(value);
        this.valueClass = value.getClass();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    Property<T> pseudoProperty() {
        PropertyClusterPseudoProperty<T> prop = new PropertyClusterPseudoProperty<>(value.get());

        pseudoProperties.add(prop);
        prop.setClusterListener((observable, oldValue, newValue) -> pseudoQueue.update(new PropertyClusterPropertyChange<T>(newValue, prop,true)));
        prop.activateClusterListener();
        return (Property<T>)prop;
    }

    public void set(T value){
        pseudoQueue.update(new PropertyClusterPropertyChange<>(value, null, false));

        System.out.println("normal set");

        pseudoProperties.forEach(pseudoProp -> {
            Platform.runLater(() -> {
                ((PropertyClusterPseudoProperty<T>)pseudoProp).deactivateClusterListener();
                pseudoProp.setValue(value);
                ((PropertyClusterPseudoProperty<T>)pseudoProp).activateClusterListener();
            });
        });

        relations.stream().sorted(Comparator.comparingInt(PropertyClusterRelation::getPriority)).forEach(rel -> rel.apply(value));
    }

    void setByRelation(T value, PropertyClusterRelation<?, T> by){
        pseudoQueue.update(new PropertyClusterPropertyChange<>(value, null, false));

        System.out.println("set by relation");

        pseudoProperties.forEach(pseudoProp -> {
            Platform.runLater(() -> {
                ((PropertyClusterPseudoProperty<T>)pseudoProp).deactivateClusterListener();
                pseudoProp.setValue(value);
                ((PropertyClusterPseudoProperty<T>)pseudoProp).activateClusterListener();
            });
        });

        relations.stream()
                .filter(rel -> rel.getTo() != by.getFrom())
                .filter(rel -> rel.getPrev().test(by.getFrom()))
                .sorted(Comparator.comparingInt(PropertyClusterRelation::getPriority))
                .forEach(rel -> rel.apply(value));
    }

    void addRelation(PropertyClusterRelation<T, ?> rel)
    {
        relations.add(rel);
    }

    AtomicReference<T> getValue(){
        return value;
    }

    public PropertyCluster getCluster() {
        return cluster;
    }

    PropertyClusterQueue<T> getPseudoQueue() {
        return pseudoQueue;
    }
}
