package top.chenbxxx.event;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;

/**
 * @author chenbxxx
 */
public class TestApplicationContextEvent extends ApplicationContextEvent {
    /**
     * Create a new ContextStartedEvent.
     *
     * @param source the {@code ApplicationContext} that the event is raised for
     *               (must not be {@code null})
     */
    public TestApplicationContextEvent(ApplicationContext source) {
        super(source);
    }
}
