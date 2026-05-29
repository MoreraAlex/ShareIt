package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.CreateUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable Long id) {
        log.info("UserController: получен запрос на получение пользователя по id={}", id);
        return UserMapper.mapUserToDto(userService.getUserById(id));
    }

    @PostMapping
    public UserDto createUser(@RequestBody @Valid CreateUserRequest userData) {
        log.info("UserController: получен запрос на добавление пользователя {}", userData);
        return UserMapper.mapUserToDto(userService.createUser(userData));
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UpdateUserRequest userData
    ) {
        log.info("UserController: получен запрос на обновление данных пользователя (id={}) {}", id, userData);
        return UserMapper.mapUserToDto(userService.updateUser(id, userData));
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        log.info("UserController: получен запрос на удаление пользователя по id={}", id);
        userService.deleteUser(id);
    }

}
