package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateRequest;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemCreateRequest;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.CreateUserRequest;
import ru.practicum.shareit.user.dto.UserResponse;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingIntegrationTests {

    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;

    private static UserResponse owner;
    private static UserResponse booker;
    private static ItemResponse item;

    @BeforeEach
    void beforeEach() {
        owner = userService.createUser(getTestOwnerData());
        booker = userService.createUser(getTestBookerData());
        item = itemService.createItem(getTestItemData(), owner.getId());
    }

    @Test
    void createBooking_test() {

        BookingCreateRequest bookingData = getTestBookingData(item.getId());
        BookingResponse booking = bookingService.createBooking(booker.getId(), bookingData);

        assertThat(booking, notNullValue());
        assertThat(booking.getId(), notNullValue());
        assertThat(booking.getStatus(), equalTo(BookingStatus.WAITING));
        assertThat(booking.getItem().getId(), equalTo(item.getId()));
        assertThat(booking.getBooker().getId(), equalTo(booker.getId()));
        assertThat(booking.getStart(), equalTo(bookingData.getStart()));
        assertThat(booking.getEnd(), equalTo(bookingData.getEnd()));

    }

    @Test
    void createBooking_whenItemIsNotAvailable_shouldThrowValidationException() {
        ItemCreateRequest itemData = new ItemCreateRequest();
        itemData.setName("item");
        itemData.setDescription("description");
        itemData.setAvailable(false);
        ItemResponse item = itemService.createItem(itemData, owner.getId());

        BookingCreateRequest bookingData = getTestBookingData(item.getId());

        assertThrows(
                ValidationException.class,
                () -> bookingService.createBooking(booker.getId(), bookingData)
        );
    }

    @Test
    void createBooking_whenUserOrItemNotFound_shouldThrowNotFoundException() {
        BookingCreateRequest bookingData = getTestBookingData(item.getId());

        assertThrows(
                NotFoundException.class,
                () -> bookingService.createBooking(999L, bookingData)
        );

        BookingCreateRequest badItemBookingData = getTestBookingData(999L);
        assertThrows(
                NotFoundException.class,
                () -> bookingService.createBooking(booker.getId(), badItemBookingData)
        );
    }

    @Test
    void approveRejectBooking_test() {

        BookingCreateRequest bookingData = getTestBookingData(item.getId());
        BookingResponse booking = bookingService.createBooking(booker.getId(), bookingData);

        assertThat(booking, notNullValue());
        assertThat(booking.getStatus(), equalTo(BookingStatus.WAITING));

        BookingResponse approvedBooking = bookingService.approveRejectBooking(owner.getId(), booking.getId(), true);

        assertThat(approvedBooking, notNullValue());
        assertThat(approvedBooking.getId(), equalTo(booking.getId()));
        assertThat(approvedBooking.getStatus(), equalTo(BookingStatus.APPROVED));
    }

    @Test
    void approveRejectBooking_byNotOwner_shouldThrowNotFoundException() {
        BookingCreateRequest bookingData = getTestBookingData(item.getId());
        BookingResponse booking = bookingService.createBooking(booker.getId(), bookingData);

        CreateUserRequest userData = new CreateUserRequest();
        userData.setName("user");
        userData.setEmail("user@test.com");
        UserResponse user = userService.createUser(userData);

        assertThrows(
                ValidationException.class,
                () -> bookingService.approveRejectBooking(user.getId(), booking.getId(), true)
        );
    }

    @Test
    void approveRejectBooking_whenBookingNotFoundOrRejected_shouldHandleCorrectly() {
        BookingCreateRequest bookingData = getTestBookingData(item.getId());
        BookingResponse booking = bookingService.createBooking(booker.getId(), bookingData);

        assertThrows(
                NotFoundException.class,
                () -> bookingService.approveRejectBooking(owner.getId(), 999L, true)
        );

        BookingResponse rejectedBooking = bookingService.approveRejectBooking(owner.getId(), booking.getId(), false);
        assertThat(rejectedBooking.getStatus(), equalTo(BookingStatus.REJECTED));
    }

    @Test
    void getBookingById_test() {

        BookingCreateRequest bookingData = getTestBookingData(item.getId());
        BookingResponse createdBooking = bookingService.createBooking(booker.getId(), bookingData);

        BookingResponse booking = bookingService.getBookingById(createdBooking.getId());

        assertThat(booking, notNullValue());
        assertThat(booking.getId(), equalTo(createdBooking.getId()));
        assertThat(booking.getStatus(), equalTo(BookingStatus.WAITING));

        assertThat(booking.getItem(), notNullValue());
        assertThat(booking.getItem().getId(), equalTo(item.getId()));
        assertThat(booking.getItem().getName(), equalTo(item.getName()));

        assertThat(booking.getBooker(), notNullValue());
        assertThat(booking.getBooker().getId(), equalTo(booker.getId()));
        assertThat(booking.getBooker().getName(), equalTo(booker.getName()));

    }

    @Test
    void getBookingById_whenNotFound_shouldThrowNotFoundException() {
        assertThrows(
                NotFoundException.class,
                () -> bookingService.getBookingById(999L)
        );
    }

    @Test
    void getBookingsOfUser_test() {

        Map<String, BookingResponse> bookings = createBookingsWithStates();

        // Тестируем фильтр ALL — должен вернуть все 5 бронирований
        Collection<BookingResponse> all = bookingService.getBookingsOfUser(booker.getId(), BookingState.ALL);
        assertThat(all, hasSize(5));

        // Тестируем фильтр CURRENT
        Collection<BookingResponse> current = bookingService.getBookingsOfUser(booker.getId(), BookingState.CURRENT);
        assertThat(current, hasSize(1));
        assertThat(current.iterator().next().getId(), equalTo(bookings.get("currentBooking").getId()));

        // Тестируем фильтр PAST
        Collection<BookingResponse> past = bookingService.getBookingsOfUser(booker.getId(), BookingState.PAST);
        assertThat(past, hasSize(1));
        assertThat(past.iterator().next().getId(), equalTo(bookings.get("pastBooking").getId()));

        // Тестируем фильтр FUTURE — сюда попадает и APPROVED на будущее, и WAITING, и REJECTED (так как у них startAfter(now))
        Collection<BookingResponse> future = bookingService.getBookingsOfUser(booker.getId(), BookingState.FUTURE);
        assertThat(future, hasSize(3));

        // Тестируем фильтр WAITING
        Collection<BookingResponse> waiting = bookingService.getBookingsOfUser(booker.getId(), BookingState.WAITING);
        assertThat(waiting, hasSize(1));
        assertThat(waiting.iterator().next().getId(), equalTo(bookings.get("waitingBooking").getId()));

        // Тестируем фильтр REJECTED
        Collection<BookingResponse> rejected = bookingService.getBookingsOfUser(booker.getId(), BookingState.REJECTED);
        assertThat(rejected, hasSize(1));
        assertThat(rejected.iterator().next().getId(), equalTo(bookings.get("rejectedBooking").getId()));

    }

    @Test
    void getBookingsOfOwner_test() {

        Map<String, BookingResponse> bookings = createBookingsWithStates();

        // 1. Тестируем фильтр ALL — владелец должен увидеть все 5 бронирований своих вещей
        Collection<BookingResponse> all = bookingService.getBookingsOfOwner(owner.getId(), BookingState.ALL);
        assertThat(all, hasSize(5));

        // 2. Тестируем фильтр CURRENT — бронирование, которое идет прямо сейчас
        Collection<BookingResponse> current = bookingService.getBookingsOfOwner(owner.getId(), BookingState.CURRENT);
        assertThat(current, hasSize(1));
        assertThat(current.iterator().next().getId(), equalTo(bookings.get("currentBooking").getId()));

        // 3. Тестируем фильтр PAST — завершенные бронирования
        Collection<BookingResponse> past = bookingService.getBookingsOfOwner(owner.getId(), BookingState.PAST);
        assertThat(past, hasSize(1));
        assertThat(past.iterator().next().getId(), equalTo(bookings.get("pastBooking").getId()));

        // 4. Тестируем фильтр FUTURE — все бронирования, которые начнутся в будущем (APPROVED, WAITING, REJECTED)
        Collection<BookingResponse> future = bookingService.getBookingsOfOwner(owner.getId(), BookingState.FUTURE);
        assertThat(future, hasSize(3));

        // 5. Тестируем фильтр WAITING — бронирования, ожидающие подтверждения владельца
        Collection<BookingResponse> waiting = bookingService.getBookingsOfOwner(owner.getId(), BookingState.WAITING);
        assertThat(waiting, hasSize(1));
        assertThat(waiting.iterator().next().getId(), equalTo(bookings.get("waitingBooking").getId()));

        // 6. Тестируем фильтр REJECTED — бронирования, которые владелец отклонил
        Collection<BookingResponse> rejected = bookingService.getBookingsOfOwner(owner.getId(), BookingState.REJECTED);
        assertThat(rejected, hasSize(1));
        assertThat(rejected.iterator().next().getId(), equalTo(bookings.get("rejectedBooking").getId()));
    }

    @Test
    void getBookings_whenUserDoesNotExist_shouldThrowNotFoundException() {
        // 1. Ошибка для getBookingsOfUser при несуществующем пользователе
        org.junit.jupiter.api.Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.getBookingsOfUser(999L, BookingState.ALL)
        );

        // 2. Ошибка для getBookingsOfOwner при несуществующем пользователе
        org.junit.jupiter.api.Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.getBookingsOfOwner(999L, BookingState.ALL)
        );
    }

    private CreateUserRequest getTestOwnerData() {
        CreateUserRequest userData = new CreateUserRequest();
        userData.setName("owner");
        userData.setEmail("owner@test.com");
        return userData;
    }

    private CreateUserRequest getTestBookerData() {
        CreateUserRequest userData = new CreateUserRequest();
        userData.setName("booker");
        userData.setEmail("booker@test.com");
        return userData;
    }

    private ItemCreateRequest getTestItemData() {
        ItemCreateRequest itemData = new ItemCreateRequest();
        itemData.setName("test item");
        itemData.setDescription("test item description");
        itemData.setAvailable(true);
        return itemData;
    }

    private BookingCreateRequest getTestBookingData(Long itemId) {
        BookingCreateRequest bookingData = new BookingCreateRequest();
        bookingData.setItemId(itemId);
        bookingData.setStart(LocalDateTime.now().plusDays(1));
        bookingData.setEnd(LocalDateTime.now().plusDays(2));
        return bookingData;
    }

    private Map<String, BookingResponse> createBookingsWithStates() {

        // 1. past booking
        BookingCreateRequest pastDto = new BookingCreateRequest();
        pastDto.setItemId(item.getId());
        pastDto.setStart(LocalDateTime.now().minusDays(5));
        pastDto.setEnd(LocalDateTime.now().minusDays(3));
        BookingResponse pastBooking = bookingService.createBooking(booker.getId(), pastDto);
        bookingService.approveRejectBooking(owner.getId(), pastBooking.getId(), true);

        // 2. current booking
        BookingCreateRequest currentDto = new BookingCreateRequest();
        currentDto.setItemId(item.getId());
        currentDto.setStart(LocalDateTime.now().minusDays(1));
        currentDto.setEnd(LocalDateTime.now().plusDays(1));
        BookingResponse currentBooking = bookingService.createBooking(booker.getId(), currentDto);
        bookingService.approveRejectBooking(owner.getId(), currentBooking.getId(), true);

        // 3. future booking
        BookingCreateRequest futureDto = new BookingCreateRequest();
        futureDto.setItemId(item.getId());
        futureDto.setStart(LocalDateTime.now().plusDays(3));
        futureDto.setEnd(LocalDateTime.now().plusDays(5));
        BookingResponse futureBooking = bookingService.createBooking(booker.getId(), futureDto);
        bookingService.approveRejectBooking(owner.getId(), futureBooking.getId(), true);

        // 4. waiting booking
        BookingCreateRequest waitingDto = new BookingCreateRequest();
        waitingDto.setItemId(item.getId());
        waitingDto.setStart(LocalDateTime.now().plusDays(10));
        waitingDto.setEnd(LocalDateTime.now().plusDays(12));
        BookingResponse waitingBooking = bookingService.createBooking(booker.getId(), waitingDto);

        // 5. rejected booking
        BookingCreateRequest rejectedDto = new BookingCreateRequest();
        rejectedDto.setItemId(item.getId());
        rejectedDto.setStart(LocalDateTime.now().plusDays(15));
        rejectedDto.setEnd(LocalDateTime.now().plusDays(17));
        BookingResponse rejectedBooking = bookingService.createBooking(booker.getId(), rejectedDto);
        bookingService.approveRejectBooking(owner.getId(), rejectedBooking.getId(), false);

        return Map.of(
                "pastBooking", pastBooking,
                "currentBooking", currentBooking,
                "futureBooking", futureBooking,
                "waitingBooking", waitingBooking,
                "rejectedBooking", rejectedBooking
        );
    }

}
