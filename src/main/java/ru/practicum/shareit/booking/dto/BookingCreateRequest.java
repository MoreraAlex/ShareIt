package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
public class BookingCreateRequest {

    @NotNull(message = "отсутствует id вещи")
    private Long itemId;

    @NotNull(message = "отсутствует дата начала бронирования")
    private LocalDateTime start;

    @NotNull(message = "отсутствует дата окончания бронирования")
    private LocalDateTime end;

    @AssertTrue(message = "дата начала бронирования не может быть в прошлом")
    public boolean isStartNotInPast() {
        return start == null || !start.isBefore(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
    }

    @AssertTrue(message = "дата окончания бронирования должна быть в будущем")
    public boolean isEndInFuture() {
        return end == null || end.isAfter(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
    }

    @AssertTrue(message = "Дата начала бронирования не может быть позже даты окончания бронирования")
    public boolean isStartBeforeEnd() {
        return start == null || end == null || start.isBefore(end);
    }

    @AssertTrue(message = "Дата начала бронирования не должна совпадать с датой окончания бронирования")
    public boolean isStartNotEqualEnd() {
        return start == null || end == null || !start.equals(end);
    }
}
