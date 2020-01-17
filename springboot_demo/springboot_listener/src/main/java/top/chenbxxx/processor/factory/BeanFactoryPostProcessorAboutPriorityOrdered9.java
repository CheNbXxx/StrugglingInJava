package top.chenbxxx.processor.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.PriorityOrdered;

/**
 * @author chenbxxx
 */
@Slf4j
public class BeanFactoryPostProcessorAboutPriorityOrdered9 implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        log.info("invoke {}",this.getClass().getSimpleName());
    }

    public int getOrder() {
        return 9;
    }

    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        log.info("invoke beanDefinitionRegistry {}",this.getClass().getSimpleName());
    }
}
