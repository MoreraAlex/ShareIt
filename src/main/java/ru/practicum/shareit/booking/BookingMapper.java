package ru.practicum.shareit.booking;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingCreateRequest;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserResponse;

@UtilityClass
public class BookingMapper {

    public ru.practicum.shareit.booking.Booking mapBookingCreateRequestToBooking(
            BookingCreateRequest bookingData,
            Item item,
            User booker,
            BookingStatus bookingStatus
    ) {
        ru.practicum.shareit.booking.Booking booking = new ru.practicum.shareit.booking.Booking();
        booking.setItem(item);
        booking.setStart(bookingData.getStart());
        booking.setEnd(bookingData.getEnd());
        booking.setBooker(booker);
        booking.setStatus(bookingStatus);
        return booking;
    }

    public BookingResponse mapBookingToBookingResponse(Booking booking, ItemResponse item, UserResponse booker) {
        BookingResponse bookingResponse = new BookingResponse();
        bookingResponse.setId(booking.getId());
        bookingResponse.setStart(booking.getStart());
        bookingResponse.setEnd(booking.getEnd());
        bookingResponse.setItem(item);
        bookingResponse.setBooker(booker);
        bookingResponse.setStatus(booking.getStatus());
        return bookingResponse;
    }

}
