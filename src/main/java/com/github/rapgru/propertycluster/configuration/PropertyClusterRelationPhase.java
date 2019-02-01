package com.github.rapgru.propertycluster.configuration;

import com.github.rapgru.propertycluster.PropertyCluster;
import com.github.rapgru.propertycluster.PropertyClusterProperty;
import com.github.rapgru.propertycluster.PropertyClusterRelation;

import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Class for specifying relations between a cluster's properties
 */
public class PropertyClusterRelationPhase {

    private PropertyCluster cluster;


    PropertyClusterRelationPhase(PropertyCluster cluster) {
        this.cluster = cluster;
    }

    /**
     * Starts the relation configuration between two cluster properties
     * @param a name of first participating cluster property
     * @param b name of first participating cluster property
     * @return object used for configuring a relation
     */
    public PropertyClusterRelationBuilder relate(String a, String b){
        return new PropertyClusterRelationBuilder(cluster.getProperties().get(a), cluster.getProperties().get(b), this);
    }

    /**
     * Finishes the property cluster configuration
     * @return a fully configured cluster, ready for work
     */
    public PropertyCluster finish(){
        return cluster;
    }

    /**
     * Class for building a relation
     */
    public class PropertyClusterRelationBuilder {
        private PropertyClusterProperty a, b;
        private PropertyClusterRelationPhase phase;
        private PropertyClusterRelationDirection direction;
        private Predicate<PropertyClusterProperty> prev;
        private BiFunction<?, PropertyCluster, ?> calculation;
        private int priority = 0;

        /**
         * Class for specifying a relation's direction
         */
        public class PropertyClusterRelationBuilderDirectionSpecifier {
            private PropertyClusterRelationBuilder relBuilder;


            private PropertyClusterRelationBuilderDirectionSpecifier(PropertyClusterRelationBuilder relBuilder) {
                this.relBuilder = relBuilder;
            }

            /**
             * set the direction so that the given cluster property is the source or "from" property
             * @param from name of the "from" cluster property
             * @return object used for configuring a relation
             * @throws PropertyClusterConfigException if the argument is none of the two names participating in the relation currently in configuration
             */
            public PropertyClusterRelationBuilder from(String from) throws PropertyClusterConfigException{
                if(a.getName().equals(from)){
                    relBuilder.setDirection(PropertyClusterRelationDirection.ATOB);
                } else if(b.getName().equals(from)) {
                     relBuilder.setDirection(PropertyClusterRelationDirection.BTOA);
                } else {
                    throw new PropertyClusterConfigException();
                }
                return relBuilder;
            }

            /**
             * set the direction so that the given cluster property is the destination or "to" property
             * @param to name of the "to" cluster property
             * @return object used for configuring a relation
             * @throws PropertyClusterConfigException if the argument is none of the two names participating in the relation currently in configuration
             */
            public PropertyClusterRelationBuilder to(String to) throws PropertyClusterConfigException{
                if(a.getName().equals(to)){
                    relBuilder.setDirection(PropertyClusterRelationDirection.BTOA);
                } else if(b.getName().equals(to)) {
                    relBuilder.setDirection(PropertyClusterRelationDirection.ATOB);
                } else {
                    throw new PropertyClusterConfigException();
                }
                return relBuilder;
            }
        }

        PropertyClusterRelationBuilder(PropertyClusterProperty a, PropertyClusterProperty b, PropertyClusterRelationPhase phase) {
            this.a = a;
            this.b = b;
            this.phase = phase;
        }

        /**
         * Specifies a relations direction
         * @return enables the specification of a relations direction
        */
        public PropertyClusterRelationBuilderDirectionSpecifier direction(){
            return new PropertyClusterRelationBuilderDirectionSpecifier(this);
        }

        /**
         * Sets the test that is done before executing the relation. The test function can use the {@link PropertyClusterProperty} from
         * which the change of this relation's "from" property originated.
         *
         * For example you specify that this relation should only execute if the name of the previous cluster property was "xyz". Then,
         * if for example the cluster property "abc" changes and triggers a relation that updates this relation's "from" property, this relation is
         * not executed. Thats because the test function will return zero for the name "abc"
         *
         * @param prev function to execute
         * @return object to continue relation configuration
         */
        public PropertyClusterRelationBuilder checkPrev(Predicate<PropertyClusterProperty> prev) {
            this.prev = prev;
            return this;
        }

        /**
         * Set the calculation that converts the "from" property value to the "to" property value
         *
         * @param calculation function that gets the updated "from" property value and the cluster instance (for getting other cluster properties' values). Returns the value that the "to" property will be set to
         * @return object to continue relation configuration
         */
        public PropertyClusterRelationBuilder calculation(BiFunction<?, PropertyCluster, ?> calculation) {
            this.calculation = calculation;
            return this;
        }

        /**
         * Sets the priority of a relation. If multiple relations originate from one cluster property, the relations with smaller priorities
         * are executed before the ones with bigger priorities. The default value is 0.
         * @param i priority
         * @return object to continue relation configuration
         */
        public PropertyClusterRelationBuilder priority(int i)
        {
            this.priority = i;
            return this;
        }

        private void setDirection(PropertyClusterRelationDirection dir){
            direction = dir;
        }

        /**
         * Ends the relation configuration
         *
         * @return object for configuring relations
         * @throws PropertyClusterConfigException if a mandatory relation setting wasn't specified
         */
        public PropertyClusterRelationPhase finishRelation() throws PropertyClusterConfigException{
            if(direction == null) throw new PropertyClusterConfigException();
            if(prev == null) throw new PropertyClusterConfigException();
            if(calculation == null) throw new PropertyClusterConfigException();
            if(direction == PropertyClusterRelationDirection.ATOB){
                PropertyClusterRelation rel = new PropertyClusterRelation(a, b, calculation, prev, priority);
                a.addRelation(rel);
            }
            if(direction == PropertyClusterRelationDirection.BTOA) {
                PropertyClusterRelation rel = new PropertyClusterRelation(b, a, calculation, prev, priority);
                b.addRelation(rel);
            }
            return phase;
        }

    }
}
