package top.chenbxxx.initializer;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import top.chenbxxx.processor.factory.BeanFactoryPostProcessorAboutOrder10;
import top.chenbxxx.processor.factory.BeanFactoryPostProcessorAboutOrder9;
import top.chenbxxx.processor.factory.BeanFactoryPostProcessorAboutPriorityOrdered10;
import top.chenbxxx.processor.factory.BeanFactoryPostProcessorAboutPriorityOrdered9;

/**
 * @author chenbxxx
 */
public class AddBeanFactoryPostProcessorsInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public void initialize(ConfigurableApplicationContext applicationContext) {
        applicationContext.addBeanFactoryPostProcessor(new BeanFactoryPostProcessorAboutOrder9());
        applicationContext.addBeanFactoryPostProcessor(new BeanFactoryPostProcessorAboutOrder10());
        applicationContext.addBeanFactoryPostProcessor(new BeanFactoryPostProcessorAboutPriorityOrdered9());
        applicationContext.addBeanFactoryPostProcessor(new BeanFactoryPostProcessorAboutPriorityOrdered10());
    }
}
