package com.mipt.nikitabumagin.processor;

import com.mipt.nikitabumagin.repository.TaskRepository;
import com.mipt.nikitabumagin.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Custom {@link BeanPostProcessor} that logs lifecycle events for task-related beans.
 *
 * <p>Intercepts the Spring container's bean initialization pipeline and emits
 * log messages <em>before</em> and <em>after</em> initialization for beans of type
 * {@link com.mipt.nikitabumagin.service.TaskService} and
 * {@link com.mipt.nikitabumagin.repository.TaskRepository}.</p>
 *
 * <p>This is useful for debugging and understanding the order in which
 * Spring initializes application components.</p>
 */
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
