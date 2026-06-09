package ru.practicum.shareit.item.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemResponseFull {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    LocalDateTime lastBooking;
    LocalDateTime nextBooking;
    List<CommentResponse> comments;
}
