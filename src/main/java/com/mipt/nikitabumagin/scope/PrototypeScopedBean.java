package com.mipt.nikitabumagin.scope;

import java.util.UUID;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class PrototypeScopedBean {

    private final String instanceId;

    public PrototypeScopedBean() {
        this.instanceId = UUID.randomUUID().toString();
    }

    /**
     * Task id generator: each new prototype bean instance produces its own unique base UUID.
     * The returned value is suitable to be used as a task identifier.
     */
    public String newTaskId() {
        return "task-" + instanceId;
    }

    public String getInstanceId() {
        return instanceId;
    }
}
