package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateRequest;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.user.dto.UserResponse;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTests {

    @MockBean
    private BookingService bookingService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void createBooking_whenRequestIsValid_shouldReturn200AndBooking() throws Exception {

        Long userId = 1L;
        Long itemId = 1L;
        Long bookingId = 1L;

        BookingCreateRequest request = new BookingCreateRequest();
        request.setItemId(itemId);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        UserResponse booker = createUserResponse(userId, "booker", "booker@test.com");
        ItemResponse item = createItemResponse(itemId, "item", "description", true);
        BookingResponse response = createBookingResponse(
            bookingId,
            request.getStart(),
            request.getEnd(),
            BookingStatus.WAITING,
            booker,
            item
        );

        when(bookingService.createBooking(eq(userId), any(BookingCreateRequest.class)))
                .thenReturn(response);

        mvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(response.getStatus().toString())))
                .andExpect(jsonPath("$.booker.id", is(booker.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(item.getId()), Long.class));
    }

    @Test
    void createBooking_whenItemOrUserNotFound_shouldReturn404() throws Exception {

        Long userId = 1L;
        BookingCreateRequest request = new BookingCreateRequest();
        request.setItemId(999L);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        String errorMessage = "Вещь с id=" + request.getItemId() + " не найдена";

        when(bookingService.createBooking(eq(userId), any(BookingCreateRequest.class)))
                .thenThrow(new NotFoundException(errorMessage));

        mvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is(errorMessage)));
    }

    @Test
    void createBooking_whenValidationFails_shouldReturn400() throws Exception {

        Long userId = 1L;
        BookingCreateRequest request = new BookingCreateRequest();
        request.setItemId(1L);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        String errorMessage = "Время начала бронирования не может быть в прошлом";

        when(bookingService.createBooking(eq(userId), any(BookingCreateRequest.class)))
                .thenThrow(new ValidationException(errorMessage));


        mvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.error", is(errorMessage)));
    }

    @Test
    void approveRejectBooking_whenRequestIsValid_shouldReturn200AndUpdatedBooking() throws Exception {

        Long ownerId = 1L;
        Long bookingId = 10L;
        boolean approved = true;

        UserResponse booker = createUserResponse(1L, "booker", "booker@test.com");
        ItemResponse item = createItemResponse(1L, "item", "description", true);
        BookingResponse response = createBookingResponse(
                bookingId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.APPROVED,
                booker,
                item
        );

        when(bookingService.approveRejectBooking(eq(ownerId), eq(bookingId), eq(approved)))
                .thenReturn(response);

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, ownerId)
                        .param("approved", String.valueOf(approved)) // Имитируем ?approved=true
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingId), Long.class))
                .andExpect(jsonPath("$.status", is(BookingStatus.APPROVED.toString())))
                .andExpect(jsonPath("$.booker.id", is(booker.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(item.getId()), Long.class));
    }

    @Test
    void approveRejectBooking_whenBookingNotFound_shouldReturn404() throws Exception {

        Long userId = 1L;
        Long bookingId = 999L;
        boolean approved = false;

        String errorMessage = "Бронирование с id=" + bookingId + " не найдено";

        when(bookingService.approveRejectBooking(eq(userId), eq(bookingId), eq(approved)))
                .thenThrow(new NotFoundException(errorMessage));

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, userId)
                        .param("approved", String.valueOf(approved))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is(errorMessage)));
    }

    @Test
    void getBookingById_whenBookingExists_shouldReturn200AndBooking() throws Exception {

        Long bookingId = 1L;

        UserResponse booker = createUserResponse(1L, "booker", "booker@test.com");
        ItemResponse item = createItemResponse(1L, "item", "description", true);
        BookingResponse response = createBookingResponse(
                bookingId,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                BookingStatus.WAITING,
                booker,
                item
        );

        when(bookingService.getBookingById(bookingId)).thenReturn(response);

        mvc.perform(get("/bookings/{bookingId}", bookingId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingId), Long.class))
                .andExpect(jsonPath("$.status", is(BookingStatus.WAITING.toString())))
                .andExpect(jsonPath("$.booker.id", is(booker.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(item.getId()), Long.class));
    }

    @Test
    void getBookingById_whenBookingDoesNotExist_shouldReturn404() throws Exception {

        Long bookingId = 999L;

        String errorMessage = "Бронирование с id=" + bookingId + " не найдено";

        when(bookingService.getBookingById(bookingId))
                .thenThrow(new NotFoundException(errorMessage));

        mvc.perform(get("/bookings/{bookingId}", bookingId)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is(errorMessage)));
    }

    @Test
    void getBookingsOfUser_whenStateIsNotProvided_shouldUseDefaultAllAndReturnCollection() throws Exception {

        Long userId = 1L;

        UserResponse booker = createUserResponse(userId, "booker", "booker@test.com");
        ItemResponse item = createItemResponse(1L, "item", "description", true);
        BookingResponse bookingResponse = createBookingResponse(
                10L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                BookingStatus.WAITING,
                booker,
                item
        );

        when(bookingService.getBookingsOfUser(eq(userId), eq(BookingState.ALL)))
                .thenReturn(java.util.List.of(bookingResponse));

        mvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(10L), Long.class))
                .andExpect(jsonPath("$[0].status", is(BookingStatus.WAITING.toString())));
    }

    @Test
    void getBookingsOfUser_whenStateIsProvided_shouldUseItAndReturnCollection() throws Exception {

        Long userId = 1L;
        BookingState state = BookingState.WAITING;

        UserResponse booker = createUserResponse(userId, "booker", "booker@test.com");
        ItemResponse item = createItemResponse(1L, "item", "description", true);
        BookingResponse bookingResponse = createBookingResponse(
                10L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                BookingStatus.WAITING,
                booker,
                item
        );

        when(bookingService.getBookingsOfUser(eq(userId), eq(state)))
                .thenReturn(java.util.List.of(bookingResponse));

        mvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .param("state", "WAITING")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(10L), Long.class));
    }

    @Test
    void getBookingsOfUser_whenUserNotFound_shouldReturn404() throws Exception {

        Long nonExistingUserId = 999L;

        String errorMessage = "Пользователь с id=" + nonExistingUserId + " не найден";

        when(bookingService.getBookingsOfUser(eq(nonExistingUserId), any(BookingState.class)))
                .thenThrow(new NotFoundException(errorMessage));

        mvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, nonExistingUserId)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is(errorMessage)));
    }

    @Test
    void getBookingsOfOwner_whenStateIsNotProvided_shouldUseDefaultAllAndReturnCollection() throws Exception {

        Long userId = 1L;

        UserResponse booker = createUserResponse(2L, "Booker", "booker@test.com");
        ItemResponse item = createItemResponse(3L, "item", "description", true);
        BookingResponse bookingResponse = createBookingResponse(
                15L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                BookingStatus.WAITING,
                booker,
                item
        );


        when(bookingService.getBookingsOfOwner(eq(userId), eq(BookingState.ALL)))
                .thenReturn(java.util.List.of(bookingResponse));

        mvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(15L), Long.class))
                .andExpect(jsonPath("$[0].status", is(BookingStatus.WAITING.toString())));
    }

    @Test
    void getBookingsOfOwner_whenStateIsProvided_shouldUseItAndReturnCollection() throws Exception {

        Long userId = 1L;
        BookingState state = BookingState.REJECTED;

        UserResponse booker = createUserResponse(2L, "Booker", "booker@test.com");
        ItemResponse item = createItemResponse(3L, "item", "description", true);
        BookingResponse bookingResponse = createBookingResponse(
                15L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                BookingStatus.REJECTED,
                booker,
                item
        );

        when(bookingService.getBookingsOfOwner(eq(userId), eq(state)))
                .thenReturn(java.util.List.of(bookingResponse));

        mvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, userId)
                        .param("state", "REJECTED")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(15L), Long.class))
                .andExpect(jsonPath("$[0].status", is(BookingStatus.REJECTED.toString())));
    }

    @Test
    void getBookingsOfOwner_whenOwnerNotFound_shouldReturn404() throws Exception {

        Long nonExistingOwnerId = 999L;

        String errorMessage = "Пользователь с id=" + nonExistingOwnerId + " не найден";

        when(bookingService.getBookingsOfOwner(eq(nonExistingOwnerId), any(BookingState.class)))
                .thenThrow(new NotFoundException(errorMessage));

        mvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, nonExistingOwnerId)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is(errorMessage)));
    }

    private UserResponse createUserResponse(Long userId, String name, String description) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(userId);
        userResponse.setName(name);
        userResponse.setEmail(description);
        return userResponse;
    }

    private ItemResponse createItemResponse(Long itemId, String name, String description, boolean available) {
        ItemResponse itemResponse = new ItemResponse();
        itemResponse.setId(itemId);
        itemResponse.setName(name);
        itemResponse.setDescription(description);
        itemResponse.setAvailable(available);
        return itemResponse;
    }

    private BookingResponse createBookingResponse(
            Long bookingId,
            LocalDateTime start,
            LocalDateTime end,
            BookingStatus status,
            UserResponse booker,
            ItemResponse item
    ) {
        BookingResponse bookingResponse = new BookingResponse();
        bookingResponse.setId(bookingId);
        bookingResponse.setStart(start);
        bookingResponse.setEnd(end);
        bookingResponse.setStatus(status);
        bookingResponse.setBooker(booker);
        bookingResponse.setItem(item);
        return bookingResponse;
    }

}
