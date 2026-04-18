package com.mipt.nikitabumagin.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.nikitabumagin.dto.v1.GatewayTaskCreateRequestDto;
import com.mipt.nikitabumagin.dto.v1.GatewayTaskResponseDto;
import com.mipt.nikitabumagin.exception.ExternalApiException;
import com.mipt.nikitabumagin.exception.TaskNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

@Component
public class ExternalTasksClient {

    private static final Logger log = LoggerFactory.getLogger(ExternalTasksClient.class);
    private static final int MAX_LOG_BODY_CHARS = 300;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public ExternalTasksClient(
            @Qualifier("externalTasksRestClient") RestClient restClient,
            ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public GatewayTaskResponseDto createTask(GatewayTaskCreateRequestDto request) {
        ResponseEntity<GatewayTaskResponseDto> response = restClient.post()
                .uri("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(),
                        (req, res) -> throwTaskNotFound(readBodySafe(res)))
                .onStatus(HttpStatusCode -> HttpStatusCode.is4xxClientError(),
                        (req, res) -> throwUnexpected4xx(res))
                .onStatus(HttpStatusCode -> HttpStatusCode.is5xxServerError(),
                        (req, res) -> throwExternalApiError(res))
                .toEntity(GatewayTaskResponseDto.class);

        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw new ExternalApiException(
                    "External API returned unexpected status for create: "
                            + response.getStatusCode().value(),
                    response.getStatusCode().value()
            );
        }

        if (response.getHeaders().getLocation() == null) {
            throw new ExternalApiException(
                    "External API did not return Location header for created task",
                    response.getStatusCode().value()
            );
        }

        GatewayTaskResponseDto body = response.getBody();
        if (body == null) {
            throw new ExternalApiException(
                    "External API returned empty body for created task",
                    response.getStatusCode().value()
            );
        }
        return body;
    }

    public GatewayTaskResponseDto getTaskById(Long id) {
        GatewayTaskResponseDto response = restClient.get()
                .uri("/tasks/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(),
                        (req, res) -> throwTaskNotFound(readBodySafe(res)))
                .onStatus(HttpStatusCode -> HttpStatusCode.is4xxClientError(),
                        (req, res) -> throwUnexpected4xx(res))
                .onStatus(HttpStatusCode -> HttpStatusCode.is5xxServerError(),
                        (req, res) -> throwExternalApiError(res))
                .body(GatewayTaskResponseDto.class);

        if (response == null) {
            throw new ExternalApiException("External API returned empty response body", 502);
        }
        return response;
    }

    public List<GatewayTaskResponseDto> listTasks(Boolean completed, int limit) {
        List<GatewayTaskResponseDto> response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tasks")
                        .queryParamIfPresent("completed", Optional.ofNullable(completed))
                        .queryParam("limit", limit)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode -> HttpStatusCode.is4xxClientError(),
                        (req, res) -> throwUnexpected4xx(res))
                .onStatus(HttpStatusCode -> HttpStatusCode.is5xxServerError(),
                        (req, res) -> throwExternalApiError(res))
                .body(new ParameterizedTypeReference<>() {
                });

        return response == null ? List.of() : response;
    }

    public void deleteTask(Long id) {
        ResponseEntity<Void> response = restClient.delete()
                .uri("/tasks/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(),
                        (req, res) -> throwTaskNotFound(readBodySafe(res)))
                .onStatus(HttpStatusCode -> HttpStatusCode.is4xxClientError(),
                        (req, res) -> throwUnexpected4xx(res))
                .onStatus(HttpStatusCode -> HttpStatusCode.is5xxServerError(),
                        (req, res) -> throwExternalApiError(res))
                .toBodilessEntity();

        if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
            throw new ExternalApiException(
                    "External API returned unexpected status for delete: "
                            + response.getStatusCode().value(),
                    response.getStatusCode().value()
            );
        }
    }

    public String callUnstable(String mode) {
        String responseBody = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/unstable")
                        .queryParam("mode", mode)
                        .build())
                .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_HTML)
                .retrieve()
                .onStatus(HttpStatusCode -> HttpStatusCode.is4xxClientError(),
                        (req, res) -> throwUnexpected4xx(res))
                .onStatus(HttpStatusCode -> HttpStatusCode.is5xxServerError(),
                        (req, res) -> throwExternalApiError(res))
                .body(String.class);

        return responseBody == null ? "" : responseBody;
    }

    private void throwTaskNotFound(String responseBody) {
        String detail = extractProblemDetail(responseBody);
        throw new TaskNotFoundException(detail);
    }

    private void throwUnexpected4xx(ClientHttpResponse response) throws IOException {
        String responseBody = readBodySafe(response);
        throw new ExternalApiException(
                "External API returned client error " + response.getStatusCode().value()
                        + ": " + truncate(responseBody),
                response.getStatusCode().value()
        );
    }

    private void throwExternalApiError(ClientHttpResponse response) throws IOException {
        String responseBody = readBodySafe(response);
        MediaType contentType = response.getHeaders().getContentType();
        int status = response.getStatusCode().value();

        if (!isJsonResponse(contentType)) {
            log.warn(
                    "External API returned unexpected content type on error. status={},"
                            + " contentType={}, bodySnippet={}",
                    status,
                    contentType,
                    truncate(responseBody)
            );
        }

        throw new ExternalApiException(
                "External API error " + status + ": " + truncate(responseBody),
                status
        );
    }

    private String extractProblemDetail(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "Task not found in external API";
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode detail = root.get("detail");
            if (detail != null && detail.isTextual() && !detail.asText().isBlank()) {
                return detail.asText();
            }
        } catch (Exception ignored) {
            // fallback to raw body below
        }
        return "Task not found in external API: " + truncate(responseBody);
    }

    private String readBodySafe(ClientHttpResponse response) {
        try {
            return StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            int status = safeStatusCode(response);
            throw new ExternalApiException(
                    "Failed to read error response from external API",
                    status,
                    ex
            );
        }
    }

    private int safeStatusCode(ClientHttpResponse response) {
        try {
            return response.getStatusCode().value();
        } catch (IOException ex) {
            return HttpStatus.BAD_GATEWAY.value();
        }
    }

    private boolean isJsonResponse(MediaType contentType) {
        return contentType != null
                && (contentType.isCompatibleWith(MediaType.APPLICATION_JSON)
                || contentType.isCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    private String truncate(String value) {
        if (value == null) {
            return "";
        }
        if (value.length() <= MAX_LOG_BODY_CHARS) {
            return value;
        }
        return value.substring(0, MAX_LOG_BODY_CHARS) + "...";
    }
}
