package ru.practicum.shareit.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class BaseClient {

    protected static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final RestTemplate restTemplate;

    public BaseClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected ResponseEntity<Object> get(String path) {
        return makeAndSendRequest(HttpMethod.GET, path, null, null, null);
    }

    protected ResponseEntity<Object> get(String path, Long userId) {
        return makeAndSendRequest(HttpMethod.GET, path, userId, null, null);
    }

    protected ResponseEntity<Object> get(String path, Long userId, Map<String, Object> parameters) {
        return makeAndSendRequest(HttpMethod.GET, path, userId, parameters, null);
    }

    protected ResponseEntity<Object> post(String path, @Nullable Object body) {
        return makeAndSendRequest(HttpMethod.POST, path, null, null, body);
    }

    protected ResponseEntity<Object> post(String path, Long userId, @Nullable Object body) {
        return makeAndSendRequest(HttpMethod.POST, path, userId, null, body);
    }

    protected ResponseEntity<Object> patch(String path, @Nullable Object body) {
        return makeAndSendRequest(HttpMethod.PATCH, path, null, null, body);
    }

    protected ResponseEntity<Object> patch(String path, Long userId, Map<String, Object> parameters,
                                           @Nullable Object body) {
        return makeAndSendRequest(HttpMethod.PATCH, path, userId, parameters, body);
    }

    protected ResponseEntity<Object> patch(String path, Long userId, @Nullable Object body) {
        return makeAndSendRequest(HttpMethod.PATCH, path, userId, null, body);
    }

    protected ResponseEntity<Object> delete(String path) {
        return makeAndSendRequest(HttpMethod.DELETE, path, null, null, null);
    }

    private ResponseEntity<Object> makeAndSendRequest(HttpMethod method, String path, @Nullable Long userId,
                                                      @Nullable Map<String, Object> parameters,
                                                      @Nullable Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (userId != null) {
            headers.set(USER_ID_HEADER, String.valueOf(userId));
        }

        HttpEntity<Object> requestEntity = new HttpEntity<>(body, headers);
        try {
            if (parameters != null) {
                return restTemplate.exchange(path, method, requestEntity, Object.class, parameters);
            }
            return restTemplate.exchange(path, method, requestEntity, Object.class);
        } catch (HttpStatusCodeException exception) {
            return ResponseEntity.status(exception.getStatusCode())
                    .headers(exception.getResponseHeaders())
                    .body(exception.getResponseBodyAsString());
        }
    }
}
