package ru.practicum.shareit.item;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.CommentResponse;
import ru.practicum.shareit.item.dto.ItemResponseFull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemResponseJsonTest {

    @Autowired
    private JacksonTester<ItemResponseFull> json;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Test
    void testItemResponseFullSerialization() throws Exception {

        LocalDateTime lastBookingTime = LocalDateTime.of(2026, 6, 1, 10, 0, 0);
        LocalDateTime nextBookingTime = LocalDateTime.of(2026, 6, 15, 14, 0, 0);
        LocalDateTime commentCreated = LocalDateTime.of(2026, 6, 3, 12, 0, 0);

        String lastBookingString = lastBookingTime.format(formatter);
        String nextBookingString = nextBookingTime.format(formatter);
        String commentCreatedString = commentCreated.format(formatter);

        CommentResponse comment = new CommentResponse();
        comment.setId(5L);
        comment.setAuthorName("author name");
        comment.setItemId(1L);
        comment.setText("comment text");
        comment.setCreated(commentCreated);

        ItemResponseFull response = new ItemResponseFull();
        response.setId(1L);
        response.setName("item");
        response.setDescription("description");
        response.setAvailable(true);
        response.setLastBooking(lastBookingTime);
        response.setNextBooking(nextBookingTime);
        response.setComments(List.of(comment));

        JsonContent<ItemResponseFull> result = json.write(response);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(response.getName());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(response.getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();

        assertThat(result).extractingJsonPathStringValue("$.lastBooking").isEqualTo(lastBookingString);
        assertThat(result).extractingJsonPathStringValue("$.nextBooking").isEqualTo(nextBookingString);

        assertThat(result).hasJsonPathArrayValue("$.comments");
        assertThat(result).extractingJsonPathNumberValue("$.comments[0].id").isEqualTo(5);
        assertThat(result).extractingJsonPathStringValue("$.comments[0].authorName").isEqualTo(comment.getAuthorName());
        assertThat(result).extractingJsonPathStringValue("$.comments[0].text").isEqualTo(comment.getText());
        assertThat(result).extractingJsonPathStringValue("$.comments[0].created").isEqualTo(commentCreatedString);
    }
}
