package design_patterns.observer;

import design_patterns.observer.simple_impl.ConcreteObserver;
import design_patterns.observer.simple_impl.ConcreteSubject;

/**
 * @author  chenbxxx
 */
public class Main {
    public static void main(String[] args) {
        ConcreteSubject concreteSubject = new ConcreteSubject();
        concreteSubject.attach(new ConcreteObserver("Jack"));
        concreteSubject.attach(new ConcreteObserver("Alice"));
        concreteSubject.notifyObserver();
    }
}
