package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingCreateRequest;
import ru.practicum.shareit.booking.dto.BookingResponse;

import java.util.Collection;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingResponse createBooking(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody @Valid BookingCreateRequest bookingData
    ) {
        log.info("BookingService: получен запрос на бронирование от пользователя {}: {}", userId, bookingData);
        return bookingService.createBooking(userId, bookingData);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponse approveRejectBooking(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId,
            @RequestParam boolean approved
    ) {
        log.info(
                "BookingService: получен запрос на одобрение/отказ по бронированию (userId = {}, bookingId = {}, approved = {})",
                userId,
                bookingId,
                approved
        );
        return bookingService.approveRejectBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponse getBookingById(@PathVariable Long bookingId) {
        log.info("BookingService: получен запрос на получение бронирования по id = {}", bookingId);
        return bookingService.getBookingById(bookingId);
    }

    @GetMapping
    public Collection<BookingResponse> getBookingsOfUser(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") BookingState bookingState
    ) {
        log.info(
                "BookingService: получен запрос на получение бронирований пользователя (userId = {}, bookingState = {})",
                userId,
                bookingState
        );
        return bookingService.getBookingsOfUser(userId, bookingState);
    }

    @GetMapping("/owner")
    public Collection<BookingResponse> getBookingsOfOwner(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") BookingState bookingState
    ) {
        log.info(
                "BookingService: получен запрос на получение бронирований вещей текущего пользователя (userId = {}, bookingState = {})",
                userId,
                bookingState
        );
        return bookingService.getBookingsOfOwner(userId, bookingState);
    }
}
