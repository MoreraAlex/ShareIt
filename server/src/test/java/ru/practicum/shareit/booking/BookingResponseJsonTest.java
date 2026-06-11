package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.user.dto.UserResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingResponseJsonTest {

    @Autowired
    private JacksonTester<BookingResponse> json;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Test
    void testBookingResponseSerialization() throws Exception {

        LocalDateTime startTime = LocalDateTime.of(2026, 7, 1, 10, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 7, 10, 18, 0, 0);

        String startString = startTime.format(formatter);
        String endString = endTime.format(formatter);

        ItemResponse item = new ItemResponse();
        item.setId(5L);
        item.setName("item");
        item.setDescription("item description");
        item.setAvailable(true);

        UserResponse booker = new UserResponse();
        booker.setId(2L);
        booker.setName("booker");
        booker.setEmail("booker@test.com");

        BookingResponse response = new BookingResponse();
        response.setId(100L);
        response.setStart(startTime);
        response.setEnd(endTime);
        response.setItem(item);
        response.setBooker(booker);
        response.setStatus(BookingStatus.APPROVED);

        JsonContent<BookingResponse> result = json.write(response);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(100);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo(BookingStatus.APPROVED.toString());

        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo(startString);
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(endString);

        assertThat(result).hasJsonPathValue("$.item");
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(5);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo(item.getName());
        assertThat(result).extractingJsonPathStringValue("$.item.description").isEqualTo(item.getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.item.available").isEqualTo(item.getAvailable());

        assertThat(result).hasJsonPathValue("$.booker");
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo(booker.getName());
        assertThat(result).extractingJsonPathStringValue("$.booker.email").isEqualTo(booker.getEmail());
    }
}
