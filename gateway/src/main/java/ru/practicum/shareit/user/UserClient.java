package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.CreateUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;

@Component
public class UserClient extends BaseClient {

    private final String serverUrl;

    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplate restTemplate) {
        super(restTemplate);
        this.serverUrl = serverUrl;
    }

    public ResponseEntity<Object> getUser(Long userId) {
        return get(serverUrl + "/users/" + userId);
    }

    public ResponseEntity<Object> createUser(CreateUserRequest userData) {
        return post(serverUrl + "/users", userData);
    }

    public ResponseEntity<Object> updateUser(Long userId, UpdateUserRequest userData) {
        return patch(serverUrl + "/users/" + userId, userData);
    }

    public ResponseEntity<Object> deleteUser(Long userId) {
        return delete(serverUrl + "/users/" + userId);
    }
}
