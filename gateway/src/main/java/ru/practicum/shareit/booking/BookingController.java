package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateRequest;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> createBooking(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody @Valid BookingCreateRequest bookingData
    ) {
        log.info("Gateway BookingController: создание бронирования пользователем {}: {}", userId, bookingData);
        return bookingClient.createBooking(userId, bookingData);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveRejectBooking(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId,
            @RequestParam Boolean approved
    ) {
        log.info("Gateway BookingController: одобрение бронирования (userId={}, bookingId={}, approved={})",
                userId, bookingId, approved);
        return bookingClient.approveRejectBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@PathVariable Long bookingId) {
        log.info("Gateway BookingController: получение бронирования id={}", bookingId);
        return bookingClient.getBookingById(bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookingsOfUser(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(name = "state", defaultValue = "ALL") BookingState bookingState
    ) {
        log.info("Gateway BookingController: бронирования пользователя (userId={}, bookingState={})",
                userId, bookingState);
        return bookingClient.getBookingsOfUser(userId, bookingState);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsOfOwner(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(name = "state", defaultValue = "ALL") BookingState bookingState
    ) {
        log.info("Gateway BookingController: бронирования владельца (userId={}, bookingState={})",
                userId, bookingState);
        return bookingClient.getBookingsOfOwner(userId, bookingState);
    }
}
