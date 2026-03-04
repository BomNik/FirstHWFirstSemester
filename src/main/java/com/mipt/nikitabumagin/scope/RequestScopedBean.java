package com.mipt.nikitabumagin.scope;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Request-scoped bean that captures a unique identifier and timestamp for each HTTP request.
 *
 * <p>Spring creates a new instance of this bean for every incoming HTTP
 * request and destroys it once the request completes. Within the same request, all injections
 * resolve to the identical instance.</p>
 *
 * @see org.springframework.web.context.annotation.RequestScope
 */
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
