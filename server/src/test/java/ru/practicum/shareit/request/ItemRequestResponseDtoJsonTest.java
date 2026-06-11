package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseItemDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestResponseDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestResponseDto> json;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Test
    void testItemRequestResponseDtoSerialization() throws Exception {

        LocalDateTime createdTime = LocalDateTime.of(2026, 6, 4, 19, 0, 0);
        String createdString = createdTime.format(formatter);

        ItemRequestResponseItemDto item = new ItemRequestResponseItemDto();
        item.setId(10L);
        item.setName("item");
        item.setOwnerId(2L);

        ItemRequestResponseDto response = new ItemRequestResponseDto();
        response.setId(1L);
        response.setDescription("request description");
        response.setCreated(createdTime);
        response.setItems(List.of(item));

        JsonContent<ItemRequestResponseDto> result = json.write(response);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(response.getDescription());

        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(createdString);

        assertThat(result).hasJsonPathArrayValue("$.items");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(10);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo(item.getName());
        assertThat(result).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(2);
    }
}
