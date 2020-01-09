package design_patterns.observer;

/**
 * 观察者模式 - 被观察者抽象
 * @author chenbxxx
 */
public interface Subject {
    void attach(Observer observer);

    void detach(Observer observer);

    void notifyObserver();
}
