package com.segurapass.service.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.segurapass.exception.SdkException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

public class ApiClient {
    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final String baseUrl;
    private final int requestTimeout;
    private final int maxRetries;
    private final long retryCooldownMillis;

    public ApiClient(String baseUrl) {
        this(baseUrl, 10, 30, 3, 1000);
    }

    public ApiClient(String baseUrl,
                     int connectionTimeout,
                     int requestTimeout,
                     int maxRetries,
                     long retryCooldownMillis) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectionTimeout))
                .build();
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .findAndRegisterModules();
        this.baseUrl = baseUrl;
        this.requestTimeout = requestTimeout;
        this.maxRetries = maxRetries;
        this.retryCooldownMillis = retryCooldownMillis;
    }

    public <T> ApiResponse<T> sendGetRequest(String path,
                               Map<String, String> queryParams,
                               String jwt,
                               Map<String, String> extraHeaders,
                               Class<T> responseType) throws SdkException {
        String httpMethod = "GET";

        HttpRequest request = baseRequest(path, queryParams, jwt, extraHeaders)
                .GET()
                .build();

        return send(request, path, httpMethod, responseType);
    }

    public <T> ApiResponse<T> sendGetRequest(String path,
                                Map<String, String> queryParams,
                                String jwt,
                                Map<String, String> extraHeaders,
                                TypeReference<T> responseType) throws SdkException {
        String httpMethod = "GET";

        HttpRequest request = baseRequest(path, queryParams, jwt, extraHeaders)
                .GET()
                .build();

        return send(request, path, httpMethod, responseType);
    }

    public <T> ApiResponse<T> sendPostRequest(Object dto,
                                String path,
                                Map<String, String> queryParams,
                                String jwt,
                                Map<String, String> extraHeaders,
                                Class<T> responseType) throws SdkException {
        String httpMethod = "POST";

        HttpRequest request = baseRequest(path, queryParams, jwt, extraHeaders)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(toJsonBody(dto, path, httpMethod)))
                .build();

        return send(request, path, httpMethod, responseType);
    }

    public <T> ApiResponse<T> sendPutRequest(Object dto,
                               String path,
                               Map<String, String> queryParams,
                               String jwt,
                               Map<String, String> extraHeaders,
                               Class<T> responseType) throws SdkException {
        String httpMethod = "PUT";

        HttpRequest request = baseRequest(path, queryParams, jwt, extraHeaders)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(toJsonBody(dto, path, httpMethod)))
                .build();

        return send(request, path, httpMethod, responseType);
    }

    public <T> ApiResponse<T> sendDeleteRequest(String path,
                                  Map<String, String> queryParams,
                                  String jwt,
                                  Map<String, String> extraHeaders,
                                  Class<T> responseType) throws SdkException {
        String httpMethod = "DELETE";

        HttpRequest request = baseRequest(path, queryParams, jwt, extraHeaders)
                .DELETE()
                .build();

        return send(request, path, httpMethod, responseType);
    }

    private HttpRequest.Builder baseRequest(String path,
                                            Map<String, String> queryParams,
                                            String jwt,
                                            Map<String, String> extraHeaders) {
        String query = "";
        if (queryParams != null && !queryParams.isEmpty()) {
            query = queryParams.entrySet().stream()
                    .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" +
                            URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&", "?", ""));
        }

        String fullPath = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;

        fullPath += path.startsWith("/") ? path : "/" + path;

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(fullPath + query))
                .timeout(Duration.ofSeconds(requestTimeout));

        if (jwt != null && !jwt.isBlank()) {
            builder.header("Authorization", "Bearer " + jwt);
        }

        if (extraHeaders != null) {
            extraHeaders.forEach(builder::header);
        }

        return builder;
    }

    private String toJsonBody(Object dto, String path, String httpMethod) {
        try {
            return mapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            String message = "Failed to write JSON body";
            throw new SdkException(0, httpMethod, message, path, e);
        }
    }

    private <T> T parseResponse(String body, Class<T> responseType, String path, String httpMethod) {
        try {
            if (body == null || body.isBlank()) {
                return null;
            }
            return mapper.readValue(body, responseType);
        } catch (JsonProcessingException e) {
            String message = "Failed to parse JSON response";
            throw new SdkException(0, httpMethod, message, path, e);
        }
    }

    private <T> T parseResponse(String body, TypeReference<T> responseType, String path, String httpMethod) {
        try {
            if (body == null || body.isBlank()) {
                return null;
            }
            return mapper.readValue(body, responseType);
        } catch (JsonProcessingException e) {
            String message = "Failed to parse JSON response";
            throw new SdkException(0, httpMethod, message, path, e);
        }
    }

    private HttpResponse<String> executeWithRetry(HttpRequest request, String path, String httpMethod) throws SdkException {
        int attempt = 0;
        long retryCooldownMillis = this.retryCooldownMillis;
        boolean idempotent = httpMethod.equals("GET") || httpMethod.equals("DELETE");

        while (true) {
            attempt++;
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                int statusCode = response.statusCode();
                String body = response.body();

                if (statusCode >= 200 && statusCode < 300) {
                    return response;
                }

                // Retry on server errors (5xx), otherwise throw
                if (statusCode >= 500 && attempt < maxRetries && idempotent) {
                    long jitter = (long)(Math.random() * 200);
                    Thread.sleep(retryCooldownMillis + jitter);
                    retryCooldownMillis = Math.min(retryCooldownMillis * 2, 30000);  // exponential backoff
                    continue;
                }

                throw new SdkException(statusCode, httpMethod, body, path);

            } catch (IOException | InterruptedException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }

                // Retry on IO failures, up to maxRetries
                if (attempt < maxRetries && idempotent) {
                    long jitter = (long)(Math.random() * 200);
                    try { Thread.sleep(retryCooldownMillis + jitter); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
                    retryCooldownMillis = Math.min(retryCooldownMillis * 2, 30000);
                    continue;
                }

                String message = "HTTP request failed";
                throw new SdkException(0, httpMethod, message, path, e);
            }
        }
    }

    private <T> ApiResponse<T> send(
            HttpRequest request,
            String path,
            String httpMethod,
            Class<T> responseType
    ) {

        HttpResponse<String> response = executeWithRetry(request, path, httpMethod);

        T body = parseResponse(response.body(), responseType, path, httpMethod);
        HttpHeaders headers = response.headers();
        int statusCode = response.statusCode();

        return new ApiResponse<>(body, headers, statusCode);
    }

    private <T> ApiResponse<T> send(
            HttpRequest request,
            String path,
            String httpMethod,
            TypeReference<T> responseType
    ) {

        HttpResponse<String> response = executeWithRetry(request, path, httpMethod);

        T body = parseResponse(response.body(), responseType, path, httpMethod);
        HttpHeaders headers = response.headers();
        int statusCode = response.statusCode();

        return new ApiResponse<>(body, headers, statusCode);
    }
}
