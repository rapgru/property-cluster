package com.github.rapgru.propertycluster;

public class PropertyClusterRegisterPhase {

    private PropertyCluster cluster = new PropertyCluster();

    /*public <T> PropertyClusterRegisterPhase add(ObservableValue<T> property, String name){
        properties.put(name, new PropertyClusterProperty(property, property.getClass(), name));
        return this;
    }

    public <T> PropertyClusterRegisterPhase add(Property<T> property) throws NoSuchFieldException {
        if(property.getName().isEmpty()){
            throw new NoSuchFieldException();
        } else {
            properties.put(property.getName(), new PropertyClusterProperty(property, property.getClass(), property.getName()));
        }
        return this;
    }*/

    public <T> PropertyClusterRegisterPhase add(String name, T initVal){
        cluster.getProperties().put(name, new PropertyClusterProperty<>(name, initVal, cluster));
        return this;
    }

    public PropertyClusterRelationPhase next(){
        return new PropertyClusterRelationPhase(cluster);
    }
}
