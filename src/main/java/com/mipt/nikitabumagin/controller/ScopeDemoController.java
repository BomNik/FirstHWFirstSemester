package com.mipt.nikitabumagin.controller;

import com.mipt.nikitabumagin.scope.PrototypeScopedBean;
import com.mipt.nikitabumagin.scope.RequestScopedBean;
import com.mipt.nikitabumagin.service.PrototypeBeanService;
import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that demonstrates Spring bean scopes.
 *
 * <p>Exposes endpoints to illustrate the behavioral difference between
 * <em>request-scoped</em> and <em>prototype-scoped</em> beans:
 * <ul>
 *   <li>{@code GET /api/scope/request} — shows that the same bean instance
 *       is reused within a single HTTP request.</li>
 *   <li>{@code GET /api/scope/prototype} — shows that every retrieval from
 *       the {@link org.springframework.beans.factory.ObjectProvider} yields
 *       a new bean instance.</li>
 * </ul>
 *
 * @see com.mipt.nikitabumagin.scope.RequestScopedBean
 * @see com.mipt.nikitabumagin.scope.PrototypeScopedBean
 */
@RestController
@RequestMapping("/api/scope")
public class ScopeDemoController {

    private final RequestScopedBean requestScopedBean;
    private final PrototypeBeanService prototypeBeanService;

    public ScopeDemoController(RequestScopedBean requestScopedBean,
            PrototypeBeanService prototypeBeanService) {
        this.requestScopedBean = requestScopedBean;
        this.prototypeBeanService = prototypeBeanService;
    }

    /**
     * Demonstrates request scope: within a single HTTP request the bean instance is the same, but
     * across different HTTP requests Spring creates a new instance.
     */
    @GetMapping("/request")
    public Map<String, Object> requestScope() {
        String id1 = requestScopedBean.getRequestId();
        Instant startedAt1 = requestScopedBean.getStartedAt();

        // Second read within the same request must match the first one.
        String id2 = requestScopedBean.getRequestId();
        Instant startedAt2 = requestScopedBean.getStartedAt();

        return Map.of(
                "requestId1", id1,
                "requestId2", id2,
                "sameInstanceWithinRequest", id1.equals(id2) && startedAt1.equals(startedAt2),
                "startedAt", startedAt1.toString());
    }

    /**
     * Demonstrates prototype scope: every provider.getObject() returns a new bean instance.
     */
    @GetMapping("/prototype")
    public Map<String, Object> prototypeScope() {
        PrototypeScopedBean b1 = prototypeBeanService.newPrototypeBean();
        PrototypeScopedBean b2 = prototypeBeanService.newPrototypeBean();

        return Map.of(
                "instanceId1", b1.getInstanceId(),
                "instanceId2", b2.getInstanceId(),
                "taskId1", b1.newTaskId(),
                "taskId2", b2.newTaskId(),
                "differentInstances", !b1.getInstanceId().equals(b2.getInstanceId()));
    }
}
