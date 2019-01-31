package com.github.rapgru.propertycluster;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public class PropertyClusterRelation<A, B> {
    private PropertyClusterProperty<A> from;
    private PropertyClusterProperty<B> to;
    private BiFunction<A, PropertyCluster, B> converter;
    private Predicate<PropertyClusterProperty> prev;
    private int priority;

    public PropertyClusterRelation(PropertyClusterProperty<A> from, PropertyClusterProperty<B> to, BiFunction<A, PropertyCluster, B> converter, Predicate<PropertyClusterProperty> prev, int priority){
        this.from = from;
        this.to = to;
        this.converter = converter;
        this.prev = prev;
        this.priority = priority;
    }

    public void apply(A newValue){
        System.out.println("Applying Relation from " + from.getName() + " (" + newValue + ") to " + to.getName() + " (" + to.getValue().get() + ")");
        to.setByRelation(converter.apply(newValue, from.getCluster()), this);
        System.out.println("Applied Relation from " + from.getName() + " (" + newValue + ") to " + to.getName() + " (" + to.getValue().get() + ")");
    }

    public PropertyClusterProperty<A> getFrom() {
        return from;
    }

    public PropertyClusterProperty<B> getTo() {
        return to;
    }

    public Predicate<PropertyClusterProperty> getPrev() {
        return prev;
    }

    public int getPriority() {
        return priority;
    }
}
