package com.mipt.nikitabumagin.service.v1;

import com.mipt.nikitabumagin.dto.v1.GatewayTaskCreateRequestDto;
import com.mipt.nikitabumagin.dto.v1.GatewayTaskResponseDto;
import com.mipt.nikitabumagin.exception.TaskNotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

/**
 * Temporary in-memory service for contract testing.
 *
 * <p>Will be replaced with RestClient-based gateway in the next stage.
 */
@Service
public class GatewayTaskContractService {

    private final Map<Long, GatewayTaskResponseDto> storage = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    public GatewayTaskResponseDto create(GatewayTaskCreateRequestDto request) {
        long id = sequence.incrementAndGet();
        GatewayTaskResponseDto task = new GatewayTaskResponseDto(
                id,
                request.title(),
                request.description(),
                request.completed() != null && request.completed()
        );
        storage.put(id, task);
        return task;
    }

    public GatewayTaskResponseDto getById(Long id) {
        GatewayTaskResponseDto task = storage.get(id);
        if (task == null) {
            throw new TaskNotFoundException(id);
        }
        return task;
    }

    public List<GatewayTaskResponseDto> list(Boolean completed, int limit) {
        return storage.values().stream()
                .filter(task -> completed == null || task.completed() == completed)
                .sorted(Comparator.comparing(GatewayTaskResponseDto::id))
                .limit(limit)
                .toList();
    }

    public void delete(Long id) {
        GatewayTaskResponseDto removed = storage.remove(id);
        if (removed == null) {
            throw new TaskNotFoundException(id);
        }
    }
}
