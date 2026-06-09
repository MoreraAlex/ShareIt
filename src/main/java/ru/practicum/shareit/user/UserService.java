package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.CreateUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserResponse;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    public UserResponse getUserById(Long id) {
        log.info("UserService: начало получения пользователя по id={}", id);
        Optional<User> userOpt = userRepository.findById(id);
        User user = userOpt.orElseThrow(() -> {
            String message = String.format("Пользователь с id=%d не найден", id);
            return new NotFoundException(message);
        });
        return UserMapper.mapUserToUserResponse(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest userData) {
        log.info("UserService: начало создания пользователя {}", userData);
        Optional<User> userFoundByEmailOpt = userRepository.findByEmail(userData.getEmail());
        if (userFoundByEmailOpt.isPresent()) {
            String message = String.format("Пользователь с email='%s' уже существует", userData.getEmail());
            throw new ValidationException(message);
        }
        User user = UserMapper.mapCreateUserDtoToUser(userData);
        userRepository.save(user);
        log.info("UserService: создан пользователь {}", user);
        return UserMapper.mapUserToUserResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest userData) {
        log.info("UserService: начало обновления данных пользователя (id={}) {}", id, userData);
        User user = userRepository.findById(id).orElseThrow(() -> {
            String message = String.format("Пользователь с id=%d не найден", id);
            return new NotFoundException(message);
        });
        if (userData.hasEmail()) {
            Optional<User> userFoundByEmailOpt = userRepository.findByEmail(userData.getEmail());
            if (userFoundByEmailOpt.isPresent() && !userFoundByEmailOpt.get().getId().equals(id)) {
                String message = String.format("Пользователь с email='%s' уже существует", userData.getEmail());
                throw new ValidationException(message);
            }
        }
        UserMapper.updateUserFields(user, userData);
        userRepository.save(user);
        log.info("UserService: обновлены данные пользователя (id={}) {}", id, userData);
        return UserMapper.mapUserToUserResponse(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("UserService: начало удаления пользователя по id={}", id);
        userRepository.deleteById(id);
        log.info("UserService: удален пользователь с id={}", id);
    }
}
