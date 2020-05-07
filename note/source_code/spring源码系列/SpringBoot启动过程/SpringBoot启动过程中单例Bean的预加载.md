# SpringBoot启动过程中单例Bean的预加载

> SpringBoot启动过程中需要调用refresh方法，refresh方法末期就会实例化所有符合条件的单例Bean

<!-- more -->

---

[TOC]

## 外部调用链

```java
// Instantiate all remaining (non-lazy-init) singletons.
beanFactory.preInstantiateSingletons();
```

以上代码就是refresh中的部分，直接委托给BeanFactory实例化单例Bean对象。





## DefaultListableBeanFactory#preInstantiateSingletons

Debug直接进入了DefaultListableBeanFactory类的preInstantiateSingletons方法。

```java
	@Override
	public void preInstantiateSingletons() throws BeansException {
        	// 常规的阶段日志
            if (logger.isDebugEnabled()) {
                	logger.debug("Pre-instantiating singletons in " + this);
            }

			// Iterate over a copy to allow for init methods which in turn register new bean definitions.
			// While this may not be part of the regular factory bootstrap, it does otherwise work fine.
        	// 所有的beanDefinittionNames装成一个List
			List<String> beanNames = new ArrayList<>(this.beanDefinitionNames);

            // Trigger initialization of all non-lazy singleton beans...
            // 遍历所有的BeanDefinitionName
            for (String beanName : beanNames) {
                    // 获取RootBeanDefinition
                    RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
                	// 判断是否符合要求
               		// 要求有三：1.不为抽象类 2.单例 3.非懒加载
                    if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {
                        	// 是否为FactoryBean
                            if (isFactoryBean(beanName)) {
                                // 如果是FactoryBean，beanName则需要加上&前缀
                                Object bean = getBean(FACTORY_BEAN_PREFIX + beanName);
                                if (bean instanceof FactoryBean) {
                                        final FactoryBean<?> factory = (FactoryBean<?>) bean;
                                        boolean isEagerInit;
                                        if (System.getSecurityManager() != null && factory instanceof SmartFactoryBean) {
                                                isEagerInit = AccessController.doPrivileged((PrivilegedAction<Boolean>)
                                                                ((SmartFactoryBean<?>) factory)::isEagerInit,
                                                        getAccessControlContext());
                                        } else {
                                                isEagerInit = (factory instanceof SmartFactoryBean &&
                                                        ((SmartFactoryBean<?>) factory).isEagerInit());
                                        }
                                        if (isEagerInit) {
                                            	getBean(beanName);
                                        }
                                    }
                            } else {
                                // 不是FactoryBean则直接获取
                                getBean(beanName);
                            }
                    }
            }

            // Trigger post-initialization callback for all applicable beans...
            for (String beanName : beanNames) {
                    Object singletonInstance = getSingleton(beanName);
                    if (singletonInstance instanceof SmartInitializingSingleton) {
                            final SmartInitializingSingleton smartSingleton = (SmartInitializingSingleton) singletonInstance;
                            if (System.getSecurityManager() != null) {
                                    AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                                        smartSingleton.afterSingletonsInstantiated();
                                        return null;
                                    }, getAccessControlContext());
                            } else {
                                	smartSingleton.afterSingletonsInstantiated();
                            }
                    }
            }
	}

```

