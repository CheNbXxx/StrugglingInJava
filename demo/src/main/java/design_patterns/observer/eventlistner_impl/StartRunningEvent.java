package design_patterns.observer.eventlistner_impl;

import java.util.EventObject;

/**
 * @author chenbxxx
 */
public class StartRunningEvent extends EventObject {
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public StartRunningEvent(Object source) {
        super(source);
    }
}
