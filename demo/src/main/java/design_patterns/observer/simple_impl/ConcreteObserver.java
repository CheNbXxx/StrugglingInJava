package design_patterns.observer.simple_impl;

import design_patterns.observer.Observer;

/**
 * @author chenbxxx
 */
public class ConcreteObserver implements Observer {

    private String name;

    public ConcreteObserver(String name){
        this.name = name;
    }

    @Override
    public void update() {
        System.out.println(name + "收到通知");
    }
}
