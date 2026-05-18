package ru.practicum.shareit.user;

import java.util.Optional;

public interface UserStorage {

    User getUserById(Long id);

    User addUser(User user);

    User updateUser(User user);

    void deleteUser(Long id);

    Optional<User> getUserByEmail(String email);

    void checkIfUserExists(Long id);

}
