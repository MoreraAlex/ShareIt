package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentCreateRequest;
import ru.practicum.shareit.item.dto.ItemCreateRequest;
import ru.practicum.shareit.item.dto.ItemUpdateRequest;

@Component
public class ItemClient extends BaseClient {

    private final String serverUrl;

    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplate restTemplate) {
        super(restTemplate);
        this.serverUrl = serverUrl;
    }

    public ResponseEntity<Object> createItem(Long userId, ItemCreateRequest itemData) {
        return post(serverUrl + "/items", userId, itemData);
    }

    public ResponseEntity<Object> updateItem(Long userId, Long itemId, ItemUpdateRequest itemData) {
        return patch(serverUrl + "/items/" + itemId, userId, itemData);
    }

    public ResponseEntity<Object> getItemById(Long userId, Long itemId) {
        return get(serverUrl + "/items/" + itemId, userId);
    }

    public ResponseEntity<Object> getItemsByOwner(Long userId) {
        return get(serverUrl + "/items", userId);
    }

    public ResponseEntity<Object> searchForItems(String text) {
        return get(serverUrl + "/items/search?text={text}", null, java.util.Map.of("text", text));
    }

    public ResponseEntity<Object> createComment(Long userId, Long itemId, CommentCreateRequest commentData) {
        return post(serverUrl + "/items/" + itemId + "/comment", userId, commentData);
    }
}
