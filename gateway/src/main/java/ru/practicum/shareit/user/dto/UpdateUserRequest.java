package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Pattern(regexp = ".*\\S.*", message = "Имя не должно быть пустым или состоять только из пробелов")
    private String name;

    @Email
    private String email;
}
