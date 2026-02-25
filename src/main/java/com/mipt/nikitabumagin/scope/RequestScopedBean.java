package com.mipt.nikitabumagin.scope;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class RequestScopedBean {

    private final String requestId;
    private final Instant startedAt;

    public RequestScopedBean() {
        this.requestId = UUID.randomUUID().toString();
        this.startedAt = Instant.now();
    }

    public String getRequestId() {
        return requestId;
    }

    public Instant getStartedAt() {
        return startedAt;
    }
}
