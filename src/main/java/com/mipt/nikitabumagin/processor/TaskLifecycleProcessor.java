package com.mipt.nikitabumagin.processor;

import com.mipt.nikitabumagin.repository.TaskRepository;
import com.mipt.nikitabumagin.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class TaskLifecycleProcessor implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(TaskLifecycleProcessor.class);

    @Override
    public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName)
            throws BeansException {
        if (isInteresting(bean)) {
            log.info("[Bean lifecycle] BEFORE initialization: beanName='{}', type={}",
                    beanName, bean.getClass().getName());
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName)
            throws BeansException {
        if (isInteresting(bean)) {
            log.info("[Bean lifecycle] AFTER initialization: beanName='{}', type={}",
                    beanName, bean.getClass().getName());
        }
        return bean;
    }

    private boolean isInteresting(Object bean) {
        // Note: after initialization Spring may wrap beans with proxies, so we check by type.
        return (bean instanceof TaskService) || (bean instanceof TaskRepository);
    }
}
