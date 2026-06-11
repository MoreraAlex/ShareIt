package ru.practicum.shareit.user;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.user.dto.CreateUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserResponse;

@UtilityClass
public class UserMapper {

    public UserResponse mapUserToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        return response;
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
