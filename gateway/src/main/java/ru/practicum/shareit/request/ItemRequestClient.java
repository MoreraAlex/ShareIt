package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Map;

@Component
public class ItemRequestClient extends BaseClient {

    private final String serverUrl;

    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplate restTemplate) {
        super(restTemplate);
        this.serverUrl = serverUrl;
    }

    public ResponseEntity<Object> createRequest(Long userId, ItemRequestDto itemRequestDto) {
        return post(serverUrl + "/requests", userId, itemRequestDto);
    }

    public ResponseEntity<Object> getOwnRequests(Long userId) {
        return get(serverUrl + "/requests", userId);
    }

    public ResponseEntity<Object> getAllRequests(Long userId, Integer from, Integer size) {
        return get(serverUrl + "/requests/all?from={from}&size={size}",
                userId, Map.of("from", from, "size", size));
    }

    public ResponseEntity<Object> getRequestById(Long userId, Long requestId) {
        return get(serverUrl + "/requests/" + requestId, userId);
    }
}
