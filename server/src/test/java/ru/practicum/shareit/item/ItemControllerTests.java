package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.dto.CommentCreateRequest;
import ru.practicum.shareit.item.dto.CommentResponse;
import ru.practicum.shareit.item.dto.ItemCreateRequest;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.item.dto.ItemResponseFull;
import ru.practicum.shareit.item.dto.ItemUpdateRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTests {

    @MockBean
    private ItemService itemService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void createItem_shouldReturnStatusOkAndValidJson() throws Exception {

        Long userId = 1L;

        ItemCreateRequest requestDto = new ItemCreateRequest();
        requestDto.setName("item");
        requestDto.setDescription("description");
        requestDto.setAvailable(true);

        ItemResponse responseDto = createItemResponse(
                1L,
                requestDto.getName(),
                requestDto.getDescription(),
                true
        );

        Mockito.when(itemService.createItem(any(ItemCreateRequest.class), eq(userId)))
                .thenReturn(responseDto);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.name").value(responseDto.getName()))
                .andExpect(jsonPath("$.description").value(responseDto.getDescription()))
                .andExpect(jsonPath("$.available").value(responseDto.getAvailable()));

        Mockito.verify(itemService, Mockito.times(1))
                .createItem(any(ItemCreateRequest.class), eq(userId));
    }

    @Test
    void createItem_whenOwnerNotFound_shouldReturn404() throws Exception {

        Long userId = 999L;
        ItemCreateRequest requestDto = new ItemCreateRequest();
        requestDto.setName("item");
        requestDto.setDescription("description");
        requestDto.setAvailable(true);

        String errorMessage = "Пользователь с id=" + userId + " не найден";

        Mockito.when(itemService.createItem(any(ItemCreateRequest.class), eq(userId)))
                .thenThrow(new NotFoundException(errorMessage));

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void createItem_whenValidationFails_shouldReturn500() throws Exception {
        Long userId = 1L;
        ItemCreateRequest requestDto = new ItemCreateRequest();
        requestDto.setName("Дрель");
        requestDto.setDescription("description");
        requestDto.setAvailable(true);

        String errorMessage = "Ошибка валидации данных вещи";

        Mockito.when(itemService.createItem(any(ItemCreateRequest.class), eq(userId)))
                .thenThrow(new ValidationException(errorMessage));

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void updateItem_shouldReturnStatusOkAndValidJson() throws Exception {

        Long userId = 1L;
        Long itemId = 10L;

        ItemUpdateRequest updateDto = new ItemUpdateRequest();
        updateDto.setName("item");
        updateDto.setDescription("description");
        updateDto.setAvailable(false);

        ItemResponse responseDto = createItemResponse(
                itemId,
                updateDto.getName(),
                updateDto.getDescription(),
                updateDto.getAvailable()
        );

        Mockito.when(itemService.updateItem(eq(userId), eq(itemId), any(ItemUpdateRequest.class)))
                .thenReturn(responseDto);

        mvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.name").value(responseDto.getName()))
                .andExpect(jsonPath("$.description").value(responseDto.getDescription()))
                .andExpect(jsonPath("$.available").value(responseDto.getAvailable()));

        Mockito.verify(itemService, Mockito.times(1))
                .updateItem(eq(userId), eq(itemId), any(ItemUpdateRequest.class));
    }

    @Test
    void updateItem_whenItemOrUserNotFound_shouldReturn404() throws Exception {

        Long userId = 1L;
        Long itemId = 999L;
        ItemUpdateRequest updateDto = new ItemUpdateRequest();
        updateDto.setName("new name");

        String errorMessage = "Вещь с id=" + itemId + " не найдена или вы не являетесь её владельцем";

        Mockito.when(itemService.updateItem(eq(userId), eq(itemId), any(ItemUpdateRequest.class)))
                .thenThrow(new NotFoundException(errorMessage));

        mvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void getItemById_whenItemExists_shouldReturnStatusOkAndValidJson() throws Exception {

        Long userId = 1L;
        Long itemId = 10L;

        CommentResponse comment = new CommentResponse();
        comment.setId(5L);
        comment.setText("comment");
        comment.setAuthorName("author");
        comment.setCreated(LocalDateTime.of(2026, 6, 1, 12, 0));

        ItemResponseFull responseDto = new ItemResponseFull();
        responseDto.setId(itemId);
        responseDto.setName("item");
        responseDto.setDescription("description");
        responseDto.setAvailable(true);
        responseDto.setLastBooking(LocalDateTime.of(2026, 5, 20, 10, 0));
        responseDto.setNextBooking(LocalDateTime.of(2026, 6, 15, 14, 0));
        responseDto.setComments(List.of(comment));

        Mockito.when(itemService.getItemById(eq(userId), eq(itemId)))
                .thenReturn(responseDto);

        mvc.perform(get("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.name").value(responseDto.getName()))
                .andExpect(jsonPath("$.description").value(responseDto.getDescription()))
                .andExpect(jsonPath("$.available").value(responseDto.getAvailable()))
                .andExpect(jsonPath("$.lastBooking").exists())
                .andExpect(jsonPath("$.nextBooking").exists())
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments[0].id").value(comment.getId()))
                .andExpect(jsonPath("$.comments[0].text").value(comment.getText()))
                .andExpect(jsonPath("$.comments[0].authorName").value(comment.getAuthorName()));

        Mockito.verify(itemService, Mockito.times(1)).getItemById(eq(userId), eq(itemId));
    }

    @Test
    void getItemById_whenItemNotFound_shouldReturn404() throws Exception {

        Long userId = 1L;
        Long itemId = 999L;
        String errorMessage = "Вещь с id=" + itemId + " не найдена";

        Mockito.when(itemService.getItemById(eq(userId), eq(itemId)))
                .thenThrow(new NotFoundException(errorMessage));

        mvc.perform(get("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void getItemsByOwner_shouldReturnStatusOkAndJsonArray() throws Exception {

        Long userId = 1L;

        ItemResponseFull itemDto = createItemResponseFull(
            10L,
            "item",
            "description",
            true,
            LocalDateTime.of(2026, 5, 20, 10, 0),
            LocalDateTime.of(2026, 6, 15, 14, 0),
            List.of()
        );

        List<ItemResponseFull> responseList = List.of(itemDto);

        Mockito.when(itemService.getItemsByOwner(eq(userId)))
                .thenReturn(responseList);

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(itemDto.getId()))
                .andExpect(jsonPath("$[0].name").value(itemDto.getName()))
                .andExpect(jsonPath("$[0].description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$[0].available").value(itemDto.getAvailable()))
                .andExpect(jsonPath("$[0].comments").isArray());

        Mockito.verify(itemService, Mockito.times(1)).getItemsByOwner(eq(userId));

    }

    @Test
    void getItemsByOwner_whenUserNotFound_shouldReturn404() throws Exception {

        Long userId = 999L;
        String errorMessage = "Пользователь с id=" + userId + " не найден";

        Mockito.when(itemService.getItemsByOwner(eq(userId)))
                .thenThrow(new NotFoundException(errorMessage));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void searchForItems_whenTextIsNotEmpty_shouldReturnStatusOkAndJsonArray() throws Exception {

        String searchText = "item";

        ItemResponse itemDto = new ItemResponse();
        itemDto.setId(1L);
        itemDto.setName("Item name");
        itemDto.setDescription("long item description");
        itemDto.setAvailable(true);

        List<ItemResponse> responseList = List.of(itemDto);

        Mockito.when(itemService.searchForItems(eq(searchText)))
                .thenReturn(responseList);

        mvc.perform(get("/items/search")
                        .param("text", searchText))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(itemDto.getId()))
                .andExpect(jsonPath("$[0].name").value(itemDto.getName()))
                .andExpect(jsonPath("$[0].description").value(itemDto.getDescription()));

        Mockito.verify(itemService, Mockito.times(1)).searchForItems(eq(searchText));
    }

    @Test
    void searchForItems_whenTextIsEmpty_shouldReturnStatusOkAndEmptyArray() throws Exception {

        String searchText = "";

        Mockito.when(itemService.searchForItems(eq(searchText)))
                .thenReturn(List.of());

        mvc.perform(get("/items/search")
                        .param("text", searchText))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        Mockito.verify(itemService, Mockito.times(1)).searchForItems(eq(searchText));
    }

    @Test
    void createComment_whenRequestIsValid_shouldReturnStatusOkAndValidJson() throws Exception {

        Long userId = 1L;
        Long itemId = 10L;

        CommentCreateRequest requestDto = new CommentCreateRequest();
        requestDto.setText("test comment text");

        CommentResponse responseDto = new CommentResponse();
        responseDto.setId(100L);
        responseDto.setText(requestDto.getText());
        responseDto.setAuthorName("author");
        responseDto.setCreated(LocalDateTime.of(2026, 6, 4, 12, 0));

        Mockito.when(itemService.createComment(eq(userId), eq(itemId), any(CommentCreateRequest.class)))
                .thenReturn(responseDto);

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.text").value(responseDto.getText()))
                .andExpect(jsonPath("$.authorName").value(responseDto.getAuthorName()))
                .andExpect(jsonPath("$.created").exists());

        Mockito.verify(itemService, Mockito.times(1))
                .createComment(eq(userId), eq(itemId), any(CommentCreateRequest.class));
    }

    @Test
    void createComment_whenItemOrUserNotFound_shouldReturn404() throws Exception {

        Long userId = 1L;
        Long itemId = 999L;
        CommentCreateRequest requestDto = new CommentCreateRequest();
        requestDto.setText("test comment text");

        String errorMessage = "Вещь с id=" + itemId + " не найдена";

        Mockito.when(itemService.createComment(eq(userId), eq(itemId), any(CommentCreateRequest.class)))
                .thenThrow(new NotFoundException(errorMessage));

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void createComment_whenUserDidNotBookItem_shouldReturn500() throws Exception {

        Long userId = 2L;
        Long itemId = 10L;
        CommentCreateRequest requestDto = new CommentCreateRequest();
        requestDto.setText("test comment text");

        String errorMessage = "Пользователь с id=" + userId + " не брал в аренду вещь с id=" + itemId;

        Mockito.when(itemService.createComment(eq(userId), eq(itemId), any(CommentCreateRequest.class)))
                .thenThrow(new ValidationException(errorMessage));

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    private ItemResponse createItemResponse(Long id, String name, String description, boolean available) {
        ItemResponse itemResponse = new ItemResponse();
        itemResponse.setId(id);
        itemResponse.setName(name);
        itemResponse.setDescription(description);
        itemResponse.setAvailable(available);
        return itemResponse;
    }

    private ItemResponseFull createItemResponseFull(
            Long id,
            String name,
            String description,
            boolean available,
            LocalDateTime lastBooking,
            LocalDateTime nextBooking,
            List<CommentResponse> comments
    ) {
        ItemResponseFull itemResponse = new ItemResponseFull();
        itemResponse.setId(id);
        itemResponse.setName(name);
        itemResponse.setDescription(description);
        itemResponse.setAvailable(available);
        itemResponse.setLastBooking(lastBooking);
        itemResponse.setNextBooking(nextBooking);
        itemResponse.setComments(comments);
        return itemResponse;
    }

}
