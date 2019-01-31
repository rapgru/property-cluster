package com.github.rapgru.propertycluster;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public class PropertyClusterRelationPhase {

    private PropertyCluster cluster;


    public PropertyClusterRelationPhase(PropertyCluster cluster) {
        this.cluster = cluster;
    }

    public PropertyClusterRelationBuilder relate(String a, String b){
        return new PropertyClusterRelationBuilder(cluster.getProperties().get(a), cluster.getProperties().get(b), this);
    }

    public PropertyCluster finish(){
        return cluster;
    }

    public class PropertyClusterRelationBuilder {
        PropertyClusterProperty a, b;
        PropertyClusterRelationPhase phase;
        PropertyClusterRelationDirection direction;
        Predicate<PropertyClusterProperty> prev;
        BiFunction<?, PropertyCluster, ?> calculation;
        int priority = 0;

        public class PropertyClusterRelationBuilderDirectionSpecifier {
            PropertyClusterRelationBuilder relBuilder;

            public PropertyClusterRelationBuilderDirectionSpecifier(PropertyClusterRelationBuilder relBuilder) {
                this.relBuilder = relBuilder;
            }

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

            /*public PropertyClusterRelationBuilder both() {
                relBuilder.setDirection(PropertyClusterRelationDirection.BOTH);
                return relBuilder;
            }*/


        }

        public PropertyClusterRelationBuilder(PropertyClusterProperty a, PropertyClusterProperty b, PropertyClusterRelationPhase phase) {
            this.a = a;
            this.b = b;
            this.phase = phase;
        }

        public PropertyClusterRelationBuilderDirectionSpecifier direction(){
            return new PropertyClusterRelationBuilderDirectionSpecifier(this);
        }

        public PropertyClusterRelationBuilder checkPrev(Predicate<PropertyClusterProperty> prev) {
            this.prev = prev;
            return this;
        }

        public PropertyClusterRelationBuilder calculation(BiFunction<?, PropertyCluster, ?> calculation) {
            this.calculation = calculation;
            return this;
        }

        public PropertyClusterRelationBuilder priority(int i)
        {
            this.priority = i;
            return this;
        }

        private void setDirection(PropertyClusterRelationDirection dir){
            direction = dir;
        }

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
