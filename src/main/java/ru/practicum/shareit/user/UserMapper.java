package ru.practicum.shareit.user;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.user.dto.CreateUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;

@UtilityClass
public class UserMapper {

    public UserDto mapUserToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        return userDto;
    }

    public User mapCreateUserDtoToUser(CreateUserRequest userData) {
        User user = new User();
        user.setName(userData.getName());
        user.setEmail(userData.getEmail());
        return user;
    }

    public void updateUserFields(User user, UpdateUserRequest userData) {

        if (userData.hasName()) {
            user.setName(userData.getName());
        }

        if (userData.hasEmail()) {
            user.setEmail(userData.getEmail());
        }

    }
}
