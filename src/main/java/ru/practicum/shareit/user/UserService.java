package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.CreateUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public User getUserById(Long id) {
        log.info("UserService: начало получения пользователя по id={}", id);
        return userStorage.getUserById(id);
    }

    public User createUser(CreateUserRequest userData) {
        log.info("UserService: начало создания пользователя {}", userData);
        Optional<User> userFoundByEmailOpt = userStorage.getUserByEmail(userData.getEmail());
        if (userFoundByEmailOpt.isPresent()) {
            String message = String.format("Пользователь с email='%s' уже существует", userData.getEmail());
            throw new ValidationException(message);
        }
        User user = UserMapper.mapCreateUserDtoToUser(userData);
        log.info("UserService: создан пользователь {}", user);
        return userStorage.addUser(user);
    }

    public User updateUser(Long id, UpdateUserRequest userData) {
        log.info("UserService: начало обновления данных пользователя (id={}) {}", id, userData);
        User user = userStorage.getUserById(id);
        if (userData.hasEmail()) {
            Optional<User> userFoundByEmailOpt = userStorage.getUserByEmail(userData.getEmail());
            if (userFoundByEmailOpt.isPresent() && !userFoundByEmailOpt.get().getId().equals(id)) {
                String message = String.format("Пользователь с email='%s' уже существует", userData.getEmail());
                throw new ValidationException(message);
            }
        }
        UserMapper.updateUserFields(user, userData);
        userStorage.updateUser(user);
        log.info("UserService: обновлены данные пользователя (id={}) {}", id, userData);
        return user;
    }

    public void deleteUser(Long id) {
        log.info("UserService: начало удаления пользователя по id={}", id);
        userStorage.deleteUser(id);
        log.info("UserService: удален пользователь с id={}", id);
    }
}
