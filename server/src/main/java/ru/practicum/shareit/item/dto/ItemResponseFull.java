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
    private Long requestId;
    private LocalDateTime lastBooking;
    private LocalDateTime nextBooking;
    private List<CommentResponse> comments;
}
