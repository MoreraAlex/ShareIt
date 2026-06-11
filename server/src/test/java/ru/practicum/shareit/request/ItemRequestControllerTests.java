package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTests {

    @MockBean
    private ItemRequestService itemRequestService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void createItemRequest_whenRequestIsValid_shouldReturnStatusOkAndValidJson() throws Exception {

        Long userId = 1L;

        ItemRequestCreateDto requestDto = new ItemRequestCreateDto();
        requestDto.setDescription("description");

        ItemRequestResponseDto responseDto = createItemRequestResponseDto(
                1L,
                requestDto.getDescription(),
                LocalDateTime.of(2026, 6, 4, 17, 0),
                List.of()
        );

        Mockito.when(itemRequestService.createItemRequest(eq(userId), any(ItemRequestCreateDto.class)))
                .thenReturn(responseDto);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.description").value(responseDto.getDescription()))
                .andExpect(jsonPath("$.created").exists())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(0));

        Mockito.verify(itemRequestService, Mockito.times(1))
                .createItemRequest(eq(userId), any(ItemRequestCreateDto.class));
    }

    @Test
    void createItemRequest_whenUserNotFound_shouldReturn404() throws Exception {

        Long userId = 999L;
        ItemRequestCreateDto requestDto = new ItemRequestCreateDto();
        requestDto.setDescription("description");

        String errorMessage = "Пользователь с id=" + userId + " не найден";

        Mockito.when(itemRequestService.createItemRequest(eq(userId), any(ItemRequestCreateDto.class)))
                .thenThrow(new NotFoundException(errorMessage));

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void createItemRequest_whenValidationFails_shouldReturn500() throws Exception {

        Long userId = 1L;
        ItemRequestCreateDto requestDto = new ItemRequestCreateDto();
        requestDto.setDescription("description");

        String errorMessage = "Описание запроса не может быть пустым";

        Mockito.when(itemRequestService.createItemRequest(eq(userId), any(ItemRequestCreateDto.class)))
                .thenThrow(new ValidationException(errorMessage));

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void getItemRequestsOfUser_shouldReturnStatusOkAndJsonArray() throws Exception {

        Long userId = 1L;

        ItemRequestResponseDto requestDto = createItemRequestResponseDto(
            10L,
            "description",
            LocalDateTime.of(2026, 6, 4, 18, 0),
            List.of()
        );

        List<ItemRequestResponseDto> responseList = List.of(requestDto);

        Mockito.when(itemRequestService.getItemRequestsOfUser(eq(userId)))
                .thenReturn(responseList);

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(requestDto.getId()))
                .andExpect(jsonPath("$[0].description").value(requestDto.getDescription()))
                .andExpect(jsonPath("$[0].created").exists())
                .andExpect(jsonPath("$[0].items").isArray());

        Mockito.verify(itemRequestService, Mockito.times(1)).getItemRequestsOfUser(eq(userId));
    }

    @Test
    void getItemRequestsOfUser_whenUserNotFound_shouldReturn404() throws Exception {

        Long userId = 999L;
        String errorMessage = "Пользователь с id=" + userId + " не найден";

        Mockito.when(itemRequestService.getItemRequestsOfUser(eq(userId)))
                .thenThrow(new NotFoundException(errorMessage));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    private ItemRequestResponseDto createItemRequestResponseDto(
            Long id,
            String descritpion,
            LocalDateTime created,
            List<ItemRequestResponseItemDto> items
    ) {
        ItemRequestResponseDto response = new ItemRequestResponseDto();
        response.setId(id);
        response.setDescription(descritpion);
        response.setCreated(created);
        response.setItems(items);
        return response;
    }

    @Test
    void getAllItemRequests_shouldReturnStatusOkAndJsonArray() throws Exception {

        Long userId = 1L;

        ItemRequestResponseDto otherUserRequestDto = createItemRequestResponseDto(
            20L,
            "description",
            LocalDateTime.of(2026, 6, 4, 15, 0),
            List.of()
        );

        List<ItemRequestResponseDto> responseList = List.of(otherUserRequestDto);

        Mockito.when(itemRequestService.getAllItemRequests(eq(userId), eq(0), eq(10)))
                .thenReturn(responseList);

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(otherUserRequestDto.getId()))
                .andExpect(jsonPath("$[0].description").value(otherUserRequestDto.getDescription()))
                .andExpect(jsonPath("$[0].created").exists())
                .andExpect(jsonPath("$[0].items").isArray());

        Mockito.verify(itemRequestService, Mockito.times(1)).getAllItemRequests(eq(userId), eq(0), eq(10));
    }

    @Test
    void getAllItemRequests_whenUserNotFound_shouldReturn404() throws Exception {

        Long userId = 999L;
        String errorMessage = "Пользователь с id=" + userId + " не найден";

        Mockito.when(itemRequestService.getAllItemRequests(eq(userId), eq(0), eq(10)))
                .thenThrow(new NotFoundException(errorMessage));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void getItemRequestById_whenRequestExists_shouldReturnStatusOkAndValidJson() throws Exception {

        Long userId = 1L;
        Long requestId = 100L;

        ItemRequestResponseDto responseDto = createItemRequestResponseDto(
            requestId,
            "description",
            LocalDateTime.of(2026, 6, 4, 12, 0),
            List.of()
        );

        Mockito.when(itemRequestService.getItemRequestById(eq(userId), eq(requestId)))
                .thenReturn(responseDto);

        mvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.description").value(responseDto.getDescription()))
                .andExpect(jsonPath("$.created").exists())
                .andExpect(jsonPath("$.items").isArray());

        Mockito.verify(itemRequestService, Mockito.times(1))
                .getItemRequestById(eq(userId), eq(requestId));
    }

    @Test
    void getItemRequestById_whenNotFound_shouldReturn404() throws Exception {

        Long userId = 1L;
        Long requestId = 999L;
        String errorMessage = "Запрос с id=" + requestId + " не найден";

        Mockito.when(itemRequestService.getItemRequestById(eq(userId), eq(requestId)))
                .thenThrow(new NotFoundException(errorMessage));

        mvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

}
