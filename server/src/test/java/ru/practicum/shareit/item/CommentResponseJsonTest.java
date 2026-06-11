package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.CommentResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentResponseJsonTest {

    @Autowired
    private JacksonTester<CommentResponse> json;

    private static final DateTimeFormatter formatter =  DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Test
    void testCommentResponseSerialization() throws Exception {

        LocalDateTime created = LocalDateTime.of(2026, 6, 4, 18, 30, 0);
        String createdString = created.format(formatter);

        CommentResponse response = new CommentResponse();
        response.setId(1L);
        response.setAuthorName("author name");
        response.setItemId(10L);
        response.setText("comment text");
        response.setCreated(created);

        JsonContent<CommentResponse> result = json.write(response);

        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);

        assertThat(result).hasJsonPathStringValue("$.authorName");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo(response.getAuthorName());

        assertThat(result).hasJsonPathNumberValue("$.itemId");
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(10);

        assertThat(result).hasJsonPathStringValue("$.text");
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo(response.getText());

        assertThat(result).hasJsonPathStringValue("$.created");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(createdString);
    }
}
