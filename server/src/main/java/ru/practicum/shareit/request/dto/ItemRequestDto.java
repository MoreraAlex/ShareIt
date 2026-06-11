package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemRequestDto {

    private Long id;

    @NotBlank
    private String description;

    private LocalDateTime created;

    private List<ItemRequestResponseItemDto> items;
}
