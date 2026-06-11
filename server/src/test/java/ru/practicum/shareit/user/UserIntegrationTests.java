package ru.practicum.shareit.user;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.CreateUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserIntegrationTests {

    private final EntityManager entityManager;
    private final UserService userService;

    @Test
    void getUserById_test() {

        User user = new User();
        user.setName("user");
        user.setEmail("user@test.com");
        entityManager.persist(user);
        entityManager.flush();

        UserResponse foundUser = userService.getUserById(user.getId());

        assertThat(foundUser, notNullValue());
        assertThat(foundUser.getId(), equalTo(user.getId()));
        assertThat(foundUser.getName(), equalTo(user.getName()));
        assertThat(foundUser.getEmail(), equalTo(user.getEmail()));

    }

    @Test
    void createUser_test() {

        CreateUserRequest userData = new CreateUserRequest();
        userData.setName("user");
        userData.setEmail("user@test.com");

        UserResponse createdUser = userService.createUser(userData);

        assertThat(createdUser, notNullValue());
        assertThat(createdUser.getId(), notNullValue());
        assertThat(createdUser.getName(), equalTo(userData.getName()));
        assertThat(createdUser.getEmail(), equalTo(userData.getEmail()));

        User userDb = entityManager
                .createQuery("select u from User u where u.email = :email", User.class)
                .setParameter("email", userData.getEmail())
                .getSingleResult();

        assertThat(userDb, notNullValue());
        assertThat(userDb.getId(), equalTo(createdUser.getId()));
        assertThat(userDb.getName(), equalTo(createdUser.getName()));
        assertThat(userDb.getEmail(), equalTo(createdUser.getEmail()));

    }

    @Test
    void createUser_whenEmailAlreadyExists_shouldThrowException() {
        User user = new User();
        user.setName("user1");
        user.setEmail("duplicate@test.com");
        entityManager.persist(user);
        entityManager.flush();

        CreateUserRequest duplicateData = new CreateUserRequest();
        duplicateData.setName("user2");
        duplicateData.setEmail("duplicate@test.com");

        assertThrows(ValidationException.class, () -> {
            userService.createUser(duplicateData);
        });
    }

    @Test
    void updateUser_whenDataIsValid_shouldUpdateAndReturnUser() {

        User user = new User();
        user.setName("user");
        user.setEmail("user@test.com");
        entityManager.persist(user);
        entityManager.flush();

        UpdateUserRequest userDataUpdate = new UpdateUserRequest();
        userDataUpdate.setName("user updated");
        userDataUpdate.setEmail("userupdated@test.com");

        UserResponse updatedUser = userService.updateUser(user.getId(), userDataUpdate);

        assertThat(updatedUser, notNullValue());
        assertThat(updatedUser.getId(), equalTo(user.getId()));
        assertThat(updatedUser.getName(), equalTo(userDataUpdate.getName()));
        assertThat(updatedUser.getEmail(), equalTo(userDataUpdate.getEmail()));

        entityManager.flush();
        entityManager.clear();

        User updatedUserDb = entityManager.find(User.class, user.getId());

        assertThat(updatedUserDb, notNullValue());
        assertThat(updatedUserDb.getName(), equalTo(userDataUpdate.getName()));
        assertThat(updatedUserDb.getEmail(), equalTo(userDataUpdate.getEmail()));

    }

    @Test
    void updateUser_whenOnlyNamePresented_shouldUpdateOnlyName() {
        User user = new User();
        user.setName("old name");
        user.setEmail("user@test.com");
        entityManager.persist(user);
        entityManager.flush();

        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName("new name");
        updateRequest.setEmail(null);

        UserResponse updatedUser = userService.updateUser(user.getId(), updateRequest);

        assertThat(updatedUser.getName(), equalTo("new name"));
        assertThat(updatedUser.getEmail(), equalTo("user@test.com"));
    }

    @Test
    void updateUser_whenOnlyEmailPresented_shouldUpdateOnlyEmail() {
        User user = new User();
        user.setName("user name");
        user.setEmail("old@test.com");
        entityManager.persist(user);
        entityManager.flush();

        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName(null);
        updateRequest.setEmail("new@test.com");

        UserResponse updatedUser = userService.updateUser(user.getId(), updateRequest);

        assertThat(updatedUser.getName(), equalTo("user name"));
        assertThat(updatedUser.getEmail(), equalTo("new@test.com"));
    }

    @Test
    void deleteUser_whenUserExists_shouldDeleteUserFromDb() {

        User user = new User();
        user.setName("user");
        user.setEmail("user@test.com");
        entityManager.persist(user);
        entityManager.flush();

        Long userId = user.getId();

        userService.deleteUser(userId);

        entityManager.flush();
        entityManager.clear();

        User deletedUserDb = entityManager.find(User.class, userId);

        assertThat(deletedUserDb, nullValue());
    }

}
