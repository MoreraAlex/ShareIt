package ru.practicum.shareit.booking;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Booking {
    private LocalDate start;
    private LocalDate end;
    private Long itemId;
    private Long userId;
}
