package ru.practicum.shareit.item.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResponse {
    private Long id;
    private String authorName;
    private Long itemId;
    private String text;
    private LocalDateTime created;
}
