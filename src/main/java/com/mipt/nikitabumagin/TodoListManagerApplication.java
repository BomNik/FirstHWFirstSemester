package com.mipt.nikitabumagin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Entry point for the Todo List Manager application.
 *
 * <p>Bootstraps the Spring Boot context with AspectJ auto-proxy support
 * enabled, allowing AOP-based cross-cutting concerns such as logging.</p>
 *
 * @see com.mipt.nikitabumagin.aspect.LoggingAspect
 */
@SpringBootApplication
@EnableAspectJAutoProxy
@EnableJpaAuditing
public class TodoListManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodoListManagerApplication.class, args);
    }

}
