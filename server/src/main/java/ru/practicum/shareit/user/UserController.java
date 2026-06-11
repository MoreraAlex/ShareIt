package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.CreateUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserResponse;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        log.info("UserController: получен запрос на получение пользователя по id={}", id);
        return userService.getUserById(id);
    }

    @PostMapping
    public UserResponse createUser(@RequestBody @Valid CreateUserRequest userData) {
        log.info("UserController: получен запрос на добавление пользователя {}", userData);
        return userService.createUser(userData);
    }

    @PatchMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UpdateUserRequest userData
    ) {
        log.info("UserController: получен запрос на обновление данных пользователя (id={}) {}", id, userData);
        return userService.updateUser(id, userData);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        log.info("UserController: получен запрос на удаление пользователя по id={}", id);
        userService.deleteUser(id);
    }

}
