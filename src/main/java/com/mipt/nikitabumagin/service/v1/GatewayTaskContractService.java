package com.mipt.nikitabumagin.service.v1;

import com.mipt.nikitabumagin.client.ExternalTasksClient;
import com.mipt.nikitabumagin.dto.v1.GatewayTaskCreateRequestDto;
import com.mipt.nikitabumagin.dto.v1.GatewayTaskResponseDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Internal gateway service that delegates task operations to external API.
 */
@Service
public class GatewayTaskContractService {

    private static final Logger log = LoggerFactory.getLogger(GatewayTaskContractService.class);
    private static final String RESILIENCE_INSTANCE = "externalApi";

    private final ExternalTasksClient externalTasksClient;

    public GatewayTaskContractService(ExternalTasksClient externalTasksClient) {
        this.externalTasksClient = externalTasksClient;
    }

    @RateLimiter(name = RESILIENCE_INSTANCE)
    @CircuitBreaker(name = RESILIENCE_INSTANCE, fallbackMethod = "createFallback")
    public GatewayTaskResponseDto create(GatewayTaskCreateRequestDto request) {
        return externalTasksClient.createTask(request);
    }

    @RateLimiter(name = RESILIENCE_INSTANCE)
    @CircuitBreaker(name = RESILIENCE_INSTANCE, fallbackMethod = "getByIdFallback")
    public GatewayTaskResponseDto getById(Long id) {
        return externalTasksClient.getTaskById(id);
    }

    @RateLimiter(name = RESILIENCE_INSTANCE)
    @CircuitBreaker(name = RESILIENCE_INSTANCE, fallbackMethod = "listFallback")
    public List<GatewayTaskResponseDto> list(Boolean completed, int limit) {
        return externalTasksClient.listTasks(completed, limit);
    }

    @RateLimiter(name = RESILIENCE_INSTANCE)
    @CircuitBreaker(name = RESILIENCE_INSTANCE, fallbackMethod = "deleteFallback")
    public void delete(Long id) {
        externalTasksClient.deleteTask(id);
    }

    @SuppressWarnings("unused")
    private GatewayTaskResponseDto createFallback(
            GatewayTaskCreateRequestDto request,
            Throwable throwable) {
        rethrowIfRateLimited(throwable);
        log.warn("Fallback create() triggered due to external API issue: {}", throwable.toString());
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "External API unavailable. Task was not created, retry later."
        );
    }

    @SuppressWarnings("unused")
    private GatewayTaskResponseDto getByIdFallback(Long id, Throwable throwable) {
        rethrowIfRateLimited(throwable);
        log.warn("Fallback getById({}) triggered due to external API issue: {}", id,
                throwable.toString());
        return new GatewayTaskResponseDto(
                id,
                "Unavailable task",
                "External API unavailable. Fallback response returned.",
                false
        );
    }

    @SuppressWarnings("unused")
    private List<GatewayTaskResponseDto> listFallback(
            Boolean completed,
            int limit,
            Throwable throwable) {
        rethrowIfRateLimited(throwable);
        log.warn("Fallback list(completed={}, limit={}) triggered due to external API issue: {}",
                completed, limit, throwable.toString());
        return List.of();
    }

    @SuppressWarnings("unused")
    private void deleteFallback(Long id, Throwable throwable) {
        rethrowIfRateLimited(throwable);
        log.warn("Fallback delete({}) triggered due to external API issue: {}", id,
                throwable.toString());
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "External API unavailable. Delete was not performed, retry later."
        );
    }

    private void rethrowIfRateLimited(Throwable throwable) {
        if (throwable instanceof RequestNotPermitted requestNotPermitted) {
            throw requestNotPermitted;
        }
        if (throwable != null
                && throwable.getCause() instanceof RequestNotPermitted requestNotPermitted) {
            throw requestNotPermitted;
        }
    }
}
