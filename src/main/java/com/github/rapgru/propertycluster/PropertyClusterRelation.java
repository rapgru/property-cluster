package com.github.rapgru.propertycluster;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public class PropertyClusterRelation<A, B> {
    private PropertyClusterProperty<A> from;
    private PropertyClusterProperty<B> to;
    private BiFunction<A, PropertyCluster, B> converter;
    private Predicate<PropertyClusterProperty> prev;
    private int priority;

    /**
     * Construct a relation between two cluster properties
     * @param from the "from" cluster property
     * @param to the "to" cluster property
     * @param converter function that converts a value from "from"-type to "to"-type
     *                  see corresponding config function {@link com.github.rapgru.propertycluster.configuration.PropertyClusterRelationPhase.PropertyClusterRelationBuilder#calculation(BiFunction)}
     * @param prev function that specifies whether a relation should execute based on the cluster property that made the "from"-property change
     *             Lets suppose that if cluster property "a" changes then, property "b" changes because of a relation bewteen them. In this case if there is another
     *             relation starting at property "b", it gets property "a" passed to the prev function
     *             see corresponding config function {@link com.github.rapgru.propertycluster.configuration.PropertyClusterRelationPhase.PropertyClusterRelationBuilder#checkPrev(Predicate)}
     * @param priority the integer priority value
     *                 see corresponding config function {@link com.github.rapgru.propertycluster.configuration.PropertyClusterRelationPhase.PropertyClusterRelationBuilder#priority(int)}
     */
    public PropertyClusterRelation(PropertyClusterProperty<A> from, PropertyClusterProperty<B> to, BiFunction<A, PropertyCluster, B> converter, Predicate<PropertyClusterProperty> prev, int priority){
        this.from = from;
        this.to = to;
        this.converter = converter;
        this.prev = prev;
        this.priority = priority;
    }

    void apply(A newValue){
        System.out.println("Applying Relation from " + from.getName() + " (" + newValue + ") to " + to.getName() + " (" + to.getValue().get() + ")");
        to.setByRelation(converter.apply(newValue, from.getCluster()), this);
        System.out.println("Applied Relation from " + from.getName() + " (" + newValue + ") to " + to.getName() + " (" + to.getValue().get() + ")");
    }

    PropertyClusterProperty<A> getFrom() {
        return from;
    }

    PropertyClusterProperty<B> getTo() {
        return to;
    }

    Predicate<PropertyClusterProperty> getPrev() {
        return prev;
    }

    int getPriority() {
        return priority;
    }
}
