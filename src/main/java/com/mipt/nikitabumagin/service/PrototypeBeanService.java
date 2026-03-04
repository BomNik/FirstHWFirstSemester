package com.mipt.nikitabumagin.service;

import com.mipt.nikitabumagin.scope.PrototypeScopedBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * Service that acts as a factory for obtaining new instances of
 * {@link com.mipt.nikitabumagin.scope.PrototypeScopedBean}.
 *
 * <p>Uses Spring's {@link org.springframework.beans.factory.ObjectProvider}
 * to request fresh prototype-scoped beans on demand, ensuring that each call to
 * {@link #newPrototypeBean()} returns a distinct instance.</p>
 */
@Service
public class PrototypeBeanService {

    private final ObjectProvider<PrototypeScopedBean> prototypeScopedBeanProvider;

    public PrototypeBeanService(ObjectProvider<PrototypeScopedBean> prototypeScopedBeanProvider) {
        this.prototypeScopedBeanProvider = prototypeScopedBeanProvider;
    }

    public PrototypeScopedBean newPrototypeBean() {
        return prototypeScopedBeanProvider.getObject();
    }
}
