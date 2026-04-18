package com.mipt.nikitabumagin.service.v1;

import com.mipt.nikitabumagin.client.ExternalTasksClient;
import com.mipt.nikitabumagin.dto.v1.GatewayTaskCreateRequestDto;
import com.mipt.nikitabumagin.dto.v1.GatewayTaskResponseDto;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Internal gateway service that delegates task operations to external API.
 */
@Service
public class GatewayTaskContractService {

    private final ExternalTasksClient externalTasksClient;

    public GatewayTaskContractService(ExternalTasksClient externalTasksClient) {
        this.externalTasksClient = externalTasksClient;
    }

    public GatewayTaskResponseDto create(GatewayTaskCreateRequestDto request) {
        return externalTasksClient.createTask(request);
    }

    public GatewayTaskResponseDto getById(Long id) {
        return externalTasksClient.getTaskById(id);
    }

    public List<GatewayTaskResponseDto> list(Boolean completed, int limit) {
        return externalTasksClient.listTasks(completed, limit);
    }

    public void delete(Long id) {
        externalTasksClient.deleteTask(id);
    }
}
