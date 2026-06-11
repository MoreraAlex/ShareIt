package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.CreateUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserResponse;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void getUserById_whenUserExists_shouldReturn200AndUser() throws Exception {

        Long userId = 1L;
        UserResponse userResponse = new UserResponse();
        userResponse.setId(userId);
        userResponse.setName("user");
        userResponse.setEmail("user@test.com");

        when(userService.getUserById(userId)).thenReturn(userResponse);

        mvc.perform(get("/users/{id}", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId), Long.class))
                .andExpect(jsonPath("$.name", is(userResponse.getName())))
                .andExpect(jsonPath("$.email", is(userResponse.getEmail())));
    }

    @Test
    void getUserById_whenUserDoesNotExist_shouldReturn404() throws Exception {

        Long userId = 999L;
        String errorMessage = "Пользователь с id=" + userId + " не найден";

        when(userService.getUserById(userId))
                .thenThrow(new NotFoundException(errorMessage));

        mvc.perform(get("/users/{id}", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is(errorMessage)));

    }

    @Test
    void createUser_whenRequestIsValid_shouldReturn200AndUser() throws Exception {

        CreateUserRequest request = new CreateUserRequest();
        request.setName("user");
        request.setEmail("user@test.com");

        UserResponse response = new UserResponse();
        response.setName(request.getName());
        response.setEmail(request.getEmail());

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenReturn(response);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(response.getName())))
                .andExpect(jsonPath("$.email", is(response.getEmail())));
    }

    @Test
    void createUser_whenEmailAlreadyExists_shouldReturn400() throws Exception {

        CreateUserRequest request = new CreateUserRequest();
        request.setName("user");
        request.setEmail("user@test.com");

        String errorMessage = "Пользователь с email='" + request.getEmail() + "' уже существует";

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new ValidationException(errorMessage));

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.error", is(errorMessage)));
    }

    @Test
    void updateUser_whenRequestIsValid_shouldReturn200AndUpdatedUser() throws Exception {

        Long userId = 1L;
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName("user updated");
        updateRequest.setEmail("userupdated@test.com");

        UserResponse response = new UserResponse();
        response.setId(userId);
        response.setName(updateRequest.getName());
        response.setEmail(updateRequest.getEmail());

        when(userService.updateUser(eq(userId), any(UpdateUserRequest.class)))
                .thenReturn(response);

        mvc.perform(patch("/users/{id}", userId)
                        .content(mapper.writeValueAsString(updateRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId), Long.class))
                .andExpect(jsonPath("$.name", is(response.getName())))
                .andExpect(jsonPath("$.email", is(response.getEmail())));
    }

    @Test
    void updateUser_whenUserNotFound_shouldReturn404() throws Exception {

        Long nonExistingId = 999L;
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName("user updated");

        String errorMessage = "Пользователь с id=" + nonExistingId + " не найден";

        when(userService.updateUser(eq(nonExistingId), any(UpdateUserRequest.class)))
                .thenThrow(new NotFoundException(errorMessage));

        mvc.perform(patch("/users/{id}", nonExistingId)
                        .content(mapper.writeValueAsString(updateRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is(errorMessage)));
    }

    @Test
    void updateUser_whenEmailAlreadyExists_shouldReturn400() throws Exception {
        Long userId = 1L;
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setEmail("existingemail@test.com");

        String errorMessage = "Пользователь с email='existingemail@test.com' уже существует";

        when(userService.updateUser(eq(userId), any(UpdateUserRequest.class)))
                .thenThrow(new ValidationException(errorMessage));

        mvc.perform(patch("/users/{id}", userId)
                        .content(mapper.writeValueAsString(updateRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.error", is(errorMessage)));
    }

    @Test
    void deleteUser_whenExecuted_shouldReturn200() throws Exception {

        Long userId = 1L;

        mvc.perform(delete("/users/{id}", userId)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk());

        Mockito.verify(userService, Mockito.times(1)).deleteUser(userId);
    }

}
