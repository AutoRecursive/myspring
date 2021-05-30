package com.fisher.service;

import com.spring.BeanPostProcessor;
import com.spring.Component;

@Component("")
public class FisherBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("Before Initialization");
        if (beanName.equals("userService")) {
            ((UserService) bean).setName("aaa");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("After Initialization");
        return bean;
    }
}
