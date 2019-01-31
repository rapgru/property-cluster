# property-cluster
A Java library for creating complex "property clusters" with **custom, bidirectional bindings**.

## Introduction
JavaFX Properties (`javafx.beans.*`) do their good, also bidirectional bindings are no problem for them. But when you want to keep big parts of program logic in properties, so everything updates automatically all the time, you may face some issues:

1. **Custom bidirectional bindings**
   If you need to bind two properties in both directions and must specify some custom code doing a transformation between the two properties types, things get tideous. StringProperties are still possible with a StringConverter ([`javafx.beans.property.StringProperty`](https://docs.oracle.com/javase/8/javafx/api/javafx/beans/property/StringProperty.html#bindBidirectional-javafx.beans.property.Property-javafx.util.StringConverter-)), but anything else needs tinkering with ChangeListeners and some mechanism to prevent infinite loops. The result of this tinkering is something like [this](http://carl-witt.de/customized-bidirectional-bindings-in-javafx/), which works but leads me to the next problem.
2. **Bind more than one property bidirectionally to a single other one**
   The standard library `bindBidirectional()` of `Property<T>` takes responsibility for controlling this behaviour. But if you need custom methods for converting between property types (and come up with the solution of problem 1) or want to have control over all these bindings, you are left with a big headache.

This is were property-cluster comes in. It lets you define cluster properties that attend a cluster, and specify bindings, so called relations, between them. The relations have many methods of execution control, which you can specify (e.g. priorities, check predecessor). The cluster properties can then be bound to other standard properties existing alongside the cluster. All this works with **totally customly written update code**, that is executed for every binding.

## Terminology
In the following table, the used terminology is explained in detail.

|Term|Explanation|
|-|-|
|cluster property|a property that is implemented in property-cluster - It can attend a binding/relation to another cluster property|
|property cluster|collection of multiple cluster properties - The cluster is responsible for shutting down threads running in cluster properties. It also stores all cluster properties|
|relation|a relation connects two cluster properties in one specific direction ("to" and "from") - It contains some custom code that is used to calculate the "to" property based on the "from" property. **If the "from" cluster property changes, the "to" cluster property is updated.**|

## Explanation by example

Lets suppose we want to develop an application that dynamically calculates the resistance of a resistor from 4 color rings and back. Therefore, when the resistance value is changed, all rings' colors must dynamically be recalculated. For the resistance value in ohms only the first 3 color rings are important, hence the 4th one is omitted in this example.

For this example, one will come up with four properties of varying types:
|Name|Type|
|-|-|
|Ring 1|Color|
|Ring 2|Color|
|Ring 3|Color|
|Resistance|Double|

The relations must be:

+ Ring 1 <-> Resistance
+ Ring 2 <-> Resistance
+ Ring 3 <-> Resistance

Note that all bindings are bidirectional.
If you look at the types in the table above you will also notice that the types for sure do not line up, and there's propably a self-written enum for a ring's color involved. You need a custom, bidirectional binding and therefore a property cluster is the ideal solution.´

### Resulting cluster configuration

#### Phase 1
First the cluster properties taking part must be specified. They consist of a String name and an initial value, from which the property type is derived. This results in these cluster properties

|Name|Type|Initial Value
|-|-|-|
|Ring 1|Color|UNDEFINED|
|Ring 2|Color|UNDEFINED|
|Ring 3|Color|UNDEFINED|
|Resistance|Double|Zero|

#### Phase 2

After this step we can setup the relations between the cluster properties. The properties of ring 1 to 3 can be summarized here, as they follow the same scheme:

|Relation|Code|
|-|-|
|Ring x -> Resistance|Calculate the resistance value from all rings (`(ring 1 * 10 + ring 2) * ring 3`, where all ring values represent the correct numbers/factors of their position as doubles)|
|Resistance -> Ring x|Calculate the color of ring x based on the resistance (formula is somewhat tricky, not shown here)

Because there are 3 rings table acutally represents **six** relations.
Controlling features for the relations are not discussed here, as they are not needed.
If you setup an cluster in these to phases and implement the relation code, everything works perfectly. No infinite loops, no problems with concurrency.

To bind the resistance value to e.g. a textbox you can simple use the `p(String name);` method on the cluster object to get a standard JavaFX property you can bind to anything else. You can create as many of these "pseudo properties" as you want, they will always be synchronized with the according cluster property.

## Installation

### Gradle
```
repositories {
	...
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.rapgru:property-cluster:v0.1.0'
}
```
### Maven
```
<repositories>
	<repository>
	    <id>jitpack.io</id>
	    <url>https://jitpack.io</url>
	</repository>
</repositories>

<dependency>
    <groupId>com.github.rapgru</groupId>
    <artifactId>property-cluster</artifactId>
    <version>Tag</version>
</dependency>
```
### SBT
```
resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies += "com.github.rapgru" % "property-cluster" % "Tag"	
```

## Configuring a cluster

Fitting the example above, there are two phases when setting up a cluster. It all starts with calling the static method `build()` of `PropertyCluster`. Then you can register cluster properties with `add()`. To continue to the next phase call the `next()` method.
Then you specify all relations one after another with `relate()` (config methods of relations listed below). To end a relations configuration call `finishRelation()`. To finish everything of call `finish()`

In the following code block you can see all possible configuration methods for each step.

```java
PropertyCluster.build()
    // Cluster Property Phase
    // .add(String propertyName, T initialvalue)
    .next()
    
    // Relation Phase
    .relate(String propertyNameA, String propertyNameB)
    
        // Relation Configuration
        .direction()
        
            // Relation Direction Configuration
            // .to(String toPropertyName)
            // .from(String fromPropertyName)
            // after .to() or .from() you automatically return to the Relation Configuration
            
        //.calculation(BiFunction<?, PropertyCluster, ?> relationCode)
        // More explanation below
        
        //.checkPrev(Predicate<PropertyClusterProperty> check)
        // More explanation below
        
        //.priority(int priority)
        // sets the relations priority, the default value is 0, lower values are executed before higher values
        // this only has an effect, if there are two or more relations going away from one cluster property
        
        .finishRelation()
        // sends you back to the relation phase and allows you to register another relation
        
    .finish()
    // leaves you with a fully configured PropertyCluster object
        
```
## Pseudo Properties / Interacting with the running cluster

To get a standard JavaFX property that is synchronized with the according cluster property simply call the `p(String name)` method on the cluster object:

```
PropertyCluster cluster = PropertyCluster.build()
    ...;
    
// Set the cluster property "aProperty" to the value 4
cluster.p("aProperty").setValue(4)

// Bind the cluster property "aPropertyOfTypeString" to a TextFields text property
TextField txt = new TextField();
txt.textProperty.bindBiDirectional((Property<String>)cluster.p("aPropertyOfTypeString"))
```

Note that in the second example you need to do an explicit type cast, as the type gets lost when all cluster properties are stored in the cluster object. As long as you register cluster properties manually you should always know which type is encapsulated in a cluster property.
