package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateRequest;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingResponse createBooking(Long bookerId, BookingCreateRequest bookingData) {
        log.info("BookingServiceImpl: начало создания бронирования пользователем id = {}: {}", bookerId, bookingData);

        User booker = userRepository.findById(bookerId).orElseThrow(() -> {
            String message = String.format("Пользователь с id=%d не найден", bookerId);
            return new NotFoundException(message);
        });

        Item item = itemRepository.findById(bookingData.getItemId()).orElseThrow(() ->  {
            String message = String.format("Вещь с id=%d не найдена", bookingData.getItemId());
            return new NotFoundException(message);
        });

        if (!item.getAvailable()) {
            String message = String.format("Вещь с id=%d недоступна для бронирования", bookingData.getItemId());
            throw new ValidationException(message);
        }

        ru.practicum.shareit.booking.Booking booking = BookingMapper.mapBookingCreateRequestToBooking(bookingData, item, booker, BookingStatus.WAITING);
        booking = bookingRepository.save(booking);
        log.info("BookingServiceImpl: бронирование успешно создано: {}", booking);
        return BookingMapper.mapBookingToBookingResponse(
                booking,
                ItemMapper.mapToItemResponse(item),
                UserMapper.mapUserToUserResponse(booker)
        );
    }

    @Override
    @Transactional
    public BookingResponse approveRejectBooking(Long ownerId, Long bookingId, boolean approved) {
        log.info(
                "BookingServiceImpl: начало одобрения/отклонения бронирования (ownerId = {}, bookingId = {}, approved = {})",
                ownerId,
                bookingId,
                approved
        );
        ru.practicum.shareit.booking.Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> {
            String message = String.format("Бронирование с id=%d не найдено", bookingId);
            return new NotFoundException(message);
        });
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            String message = String.format("Пользователь с id=%d не является владельцем вещи", ownerId);
            throw new ValidationException(message);
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        bookingRepository.save(booking);

        log.info(
                "BookingServiceImpl: окончание одобрения/отклонения бронирования (ownerId = {}, bookingId = {}, approved = {})",
                ownerId,
                bookingId,
                approved
        );

        return BookingMapper.mapBookingToBookingResponse(
                booking,
                ItemMapper.mapToItemResponse(booking.getItem()),
                UserMapper.mapUserToUserResponse(booking.getBooker())
        );
    }

    @Override
    public BookingResponse getBookingById(Long bookingId) {
        log.info("BookingServiceImpl: получение бронирования по id = {}", bookingId);
        ru.practicum.shareit.booking.Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> {
            String message = String.format("Бронирование с id=%d не найдено", bookingId);
            return new NotFoundException(message);
        });
        return BookingMapper.mapBookingToBookingResponse(
                booking,
                ItemMapper.mapToItemResponse(booking.getItem()),
                UserMapper.mapUserToUserResponse(booking.getBooker())
        );
    }

    @Override
    public Collection<BookingResponse> getBookingsOfUser(Long bookerId, BookingState bookingState) {

        log.info(
                "BookingServiceImpl: получение всех бронирований пользователя (bookerId = {}, bookingState = {})",
                bookerId,
                bookingState
        );

        if (!userRepository.existsById(bookerId)) {
            String message = String.format("Пользователь с id=%d не найден", bookerId);
            throw new NotFoundException(message);
        }

        LocalDateTime now = LocalDateTime.now();
        Sort newestFirst = Sort.by(Sort.Direction.DESC, "start");

        Collection<ru.practicum.shareit.booking.Booking> bookings = switch (bookingState) {
            case BookingState.ALL -> bookings = bookingRepository.findAllByBookerId(bookerId, newestFirst);
            case BookingState.WAITING -> bookings =
                    bookingRepository.findAllByBookerIdAndStatus(bookerId, BookingStatus.WAITING, newestFirst);
            case BookingState.REJECTED -> bookings =
                    bookingRepository.findAllByBookerIdAndStatus(bookerId, BookingStatus.REJECTED, newestFirst);
            case BookingState.CURRENT -> bookings =
                    bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfter(bookerId, now, now, newestFirst);
            case BookingState.PAST -> bookings =
                    bookingRepository.findAllByBookerIdAndEndBefore(bookerId, now, newestFirst);
            case BookingState.FUTURE -> bookings =
                    bookingRepository.findAllByBookerIdAndStartAfter(bookerId, now, newestFirst);
        };

        return bookings.stream()
                .map(booking -> BookingMapper.mapBookingToBookingResponse(
                        booking,
                        ItemMapper.mapToItemResponse(booking.getItem()),
                        UserMapper.mapUserToUserResponse(booking.getBooker())
                ))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<BookingResponse> getBookingsOfOwner(Long ownerId, BookingState bookingState) {
        log.info(
                "BookingServiceImpl: получение бронирований вещей текущего пользователя (ownerId = {}, bookingState = {})",
                ownerId,
                bookingState
        );

        if (!userRepository.existsById(ownerId)) {
            String message = String.format("Пользователь с id=%d не найден", ownerId);
            throw new NotFoundException(message);
        }

        LocalDateTime now = LocalDateTime.now();
        Sort newestFirst = Sort.by(Sort.Direction.DESC, "start");

        Collection<Booking> bookings = switch (bookingState) {
            case BookingState.ALL -> bookings = bookingRepository.findAllByItemOwnerId(ownerId, newestFirst);
            case BookingState.WAITING -> bookings =
                    bookingRepository.findAllByItemOwnerIdAndStatus(ownerId, BookingStatus.WAITING, newestFirst);
            case BookingState.REJECTED -> bookings =
                    bookingRepository.findAllByItemOwnerIdAndStatus(ownerId, BookingStatus.REJECTED, newestFirst);
            case BookingState.CURRENT -> bookings =
                    bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfter(ownerId, now, now, newestFirst);
            case BookingState.PAST -> bookings =
                    bookingRepository.findAllByItemOwnerIdAndEndBefore(ownerId, now, newestFirst);
            case BookingState.FUTURE -> bookings =
                    bookingRepository.findAllByItemOwnerIdAndStartAfter(ownerId, now, newestFirst);
        };

        return bookings.stream()
                .map(booking -> BookingMapper.mapBookingToBookingResponse(
                        booking,
                        ItemMapper.mapToItemResponse(booking.getItem()),
                        UserMapper.mapUserToUserResponse(booking.getBooker())
                ))
                .collect(Collectors.toList());
    }

}
