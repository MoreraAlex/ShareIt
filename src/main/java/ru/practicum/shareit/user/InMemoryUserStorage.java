package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private Long lastId = 0L;

    @Override
    public User getUserById(Long id) {
        log.info("InMemoryUserStorage: начало поиска пользователя по id={}", id);
        User user = users.get(id);
        if (user == null) {
            String message = String.format("Пользователь с id=%d не найден", id);
            throw new NotFoundException(message);
        }
        return user;
    }

    @Override
    public User addUser(User user) {
        log.info("InMemoryUserStorage: начало добавления пользователя {}", user);
        lastId++;
        user.setId(lastId);
        users.put(lastId, user);
        log.info("InMemoryUserStorage: успешно добавлен пользователь {}", user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void deleteUser(Long id) {
        log.info("InMemoryUserStorage: начало удаления пользователя по id={}", id);
        users.remove(id);
        log.info("InMemoryUserStorage: удален пользователь с id={}", id);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return users.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public void checkIfUserExists(Long id) {
        getUserById(id);
    }
}
