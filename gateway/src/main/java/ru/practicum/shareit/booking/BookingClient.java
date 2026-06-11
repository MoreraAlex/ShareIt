package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.booking.dto.BookingCreateRequest;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Component
public class BookingClient extends BaseClient {

    private final String serverUrl;

    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplate restTemplate) {
        super(restTemplate);
        this.serverUrl = serverUrl;
    }

    public ResponseEntity<Object> createBooking(Long userId, BookingCreateRequest bookingData) {
        return post(serverUrl + "/bookings", userId, bookingData);
    }

    public ResponseEntity<Object> approveRejectBooking(Long userId, Long bookingId, Boolean approved) {
        return patch(serverUrl + "/bookings/" + bookingId + "?approved={approved}",
                userId, Map.of("approved", approved), null);
    }

    public ResponseEntity<Object> getBookingById(Long bookingId) {
        return get(serverUrl + "/bookings/" + bookingId);
    }

    public ResponseEntity<Object> getBookingsOfUser(Long userId, BookingState bookingState) {
        return get(serverUrl + "/bookings?state={bookingState}",
                userId, Map.of("bookingState", bookingState));
    }

    public ResponseEntity<Object> getBookingsOfOwner(Long userId, BookingState bookingState) {
        return get(serverUrl + "/bookings/owner?state={bookingState}",
                userId, Map.of("bookingState", bookingState));
    }
}
