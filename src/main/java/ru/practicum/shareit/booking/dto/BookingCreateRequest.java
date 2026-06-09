package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingCreateRequest {

    @NotNull(message = "отсутствует id вещи")
    private Long itemId;

    @NotNull(message = "отсутствует дата начала бронирования")
    @FutureOrPresent(message = "дата начала бронирования не может быть в прошлом")
    private LocalDateTime start;

    @NotNull(message = "отсутствует дата окончания бронирования")
    @Future(message = "дата окончания бронирования должна быть в будущем")
    private LocalDateTime end;

    @AssertTrue(message = "Дата начала бронирования не может быть позже даты окончания бронирования")
    public boolean isStartBeforeEnd() {
        return start.isBefore(end);
    }

    @AssertTrue(message = "Дата начала бронирования не должна совпадать с датой окончания бронирования")
    public boolean isStartNotEqualEnd() {
        return !start.equals(end);
    }
}
