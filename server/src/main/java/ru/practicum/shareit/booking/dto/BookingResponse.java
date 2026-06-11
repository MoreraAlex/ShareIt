package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.user.dto.UserResponse;

import java.time.LocalDateTime;

@Data
public class BookingResponse {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private ItemResponse item;
    private UserResponse booker;
    private BookingStatus status;
}
