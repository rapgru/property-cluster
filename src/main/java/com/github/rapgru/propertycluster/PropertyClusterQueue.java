package com.github.rapgru.propertycluster;

import java.util.concurrent.LinkedBlockingQueue;

public class PropertyClusterQueue<T> {

    private PropertyClusterProperty<T> property;
    private LinkedBlockingQueue<PropertyClusterPropertyChange<T>> queue;
    private Thread worker;

    PropertyClusterQueue(PropertyClusterProperty<T> property){
        this.property = property;
        queue = new LinkedBlockingQueue<>();

        worker = new Thread(() -> {
             while(property.getCluster().getClusterUp().get()){
                 try {
                     System.out.println("Taking from queue");
                     PropertyClusterPropertyChange<T> change = queue.take();
                     System.out.println("Took from the queue");
                     if(change.isUpdateOverQueue())
                         property.set(change.getNewValue());
                     else
                         property.getValue().set(change.getNewValue());
                 } catch (InterruptedException e) {
                     System.out.println("Interrution");
                 }
             }
        });
        worker.start();
    }

    public void update(PropertyClusterPropertyChange<T> change){
        try {
            System.out.println("Putting to queue");
            queue.put(change);
            System.out.println("Put to queue");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void interruptWorker(){
        worker.interrupt();
    }

}
