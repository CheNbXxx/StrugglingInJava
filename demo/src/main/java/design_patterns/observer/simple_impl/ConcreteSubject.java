package design_patterns.observer.simple_impl;

import design_patterns.observer.Observer;
import design_patterns.observer.Subject;

import java.util.LinkedList;
import java.util.List;

/**
 * @author chenbxxx
 */
public class ConcreteSubject implements Subject {

    /**
     * 我认为的结构核心：被观察对象持有所有观察对象的引用
     */
    private List<Observer> observers = new LinkedList<>();

    @Override
    public void attach(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObserver() {
        System.out.println("ConcreteSubject is change");
        observers.forEach(Observer::update);
    }
}
