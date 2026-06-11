package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public ItemResponse createItem(ItemCreateRequest itemData, Long ownerId) {
        log.info("ItemServiceImpl: начало добавления новой вещи {} пользователем {}", itemData, ownerId);
        User owner = userRepository.findById(ownerId).orElseThrow(() -> {
            String message = String.format("Пользователь с id=%d не найден", ownerId);
            return new NotFoundException(message);
        });
        Item item = ItemMapper.mapItemCreateRequestToItem(itemData);
        item.setOwner(owner);
        if (itemData.getRequestId() != null) {
            ItemRequest itemRequest = itemRequestRepository.findById(itemData.getRequestId()).orElseThrow(() -> {
                String message = String.format("Запрос вещи с id=%d не найден", itemData.getRequestId());
                return new NotFoundException(message);
            });
            item.setRequest(itemRequest);
        }
        Item savedItem = itemRepository.save(item);
        log.info("ItemServiceImpl: добавлена новая вещь {} пользователем {}", itemData, ownerId);
        return ItemMapper.mapToItemResponse(savedItem);
    }

    @Override
    @Transactional
    public ItemResponse updateItem(Long ownerId, Long itemId, ItemUpdateRequest itemData) {
        log.info("ItemServiceImpl: начало обновления данных вещи {} (ownerId={}, itemId={})", itemData, ownerId, itemId);

        if (!userRepository.existsById(ownerId)) {
            String message = String.format("Пользователь с id=%d не найден", ownerId);
            throw new NotFoundException(message);
        }

        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            String message = String.format("вещь с id=%d не найдена", itemId);
            return new NotFoundException(message);
        });

        if (!item.getOwner().getId().equals(ownerId)) {
            String message = String.format("Пользователь %d не является владельцем вещи %d", ownerId, itemId);
            throw new NotFoundException(message);
        }
        ItemMapper.updateItemFields(item, itemData);
        itemRepository.save(item);
        log.info("ItemServiceImpl: обновлены данные вещи {} (ownerId={}, itemId={})", itemData, ownerId, itemId);
        return ItemMapper.mapToItemResponse(item);
    }

    @Override
    public ItemResponseFull getItemById(Long userId, Long itemId) {
        log.info("ItemServiceImpl: получение данных вещи по id (itemId={})", itemId);
        LocalDateTime now = LocalDateTime.now();
        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            String message = String.format("вещь с id=%d не найдена", itemId);
            return new NotFoundException(message);
        });

        LocalDateTime lastBooking = null;
        LocalDateTime nextBooking = null;

        if (item.getOwner().getId().equals(userId)) {
            Sort newestFirst = Sort.by(Sort.Direction.DESC, "start");
            Collection<Booking> itemBookings = bookingRepository.findAllByItemId(itemId, newestFirst);
            lastBooking = findLastBookingOfItem(itemBookings, now);
            nextBooking = findNextBookingOfItem(itemBookings, now);
        }
        List<CommentResponse> comments = getCommentsForItem(itemId);
        return ItemMapper.mapToItemResponseFull(item, lastBooking, nextBooking, comments);
    }

    @Override
    public Collection<ItemResponseFull> getItemsByOwner(Long ownerId) {
        log.info("ItemServiceImpl: получение вещей пользователя (ownerId={})", ownerId);
        LocalDateTime now = LocalDateTime.now();
        Collection<Item> items = itemRepository.findByOwnerId(ownerId);
        Sort newestFirst = Sort.by(Sort.Direction.DESC, "start");
        Collection<Booking> bookings = bookingRepository.findAllByItemOwnerId(ownerId, newestFirst);
        Map<Long, List<CommentResponse>> commentsByItemId = getCommentsForItems(items);
        return items.stream()
                .map((item) -> {
                    Collection<Booking> itemBookings = bookings.stream()
                            .filter(booking -> booking.getItem().getId().equals(item.getId()))
                            .toList();
                    LocalDateTime lastBooking = findLastBookingOfItem(itemBookings, now);
                    LocalDateTime nextBooking = findNextBookingOfItem(itemBookings, now);
                    List<CommentResponse> comments = commentsByItemId.getOrDefault(item.getId(), List.of());
                    return ItemMapper.mapToItemResponseFull(
                            item,
                            lastBooking,
                            nextBooking,
                            comments
                    );
                })
                .toList();
    }

    @Override
    public Collection<ItemResponse> searchForItems(String text) {
        log.info("ItemServiceImpl: поиск вещей по строке (text={})", text);
        if (text.isEmpty()) {
            return new ArrayList<>();
        }
        return itemRepository.searchForItems(text).stream()
                .map(ItemMapper::mapToItemResponse)
                .toList();
    }

    @Override
    @Transactional
    public CommentResponse createComment(Long authorId, Long itemId, CommentCreateRequest commentData) {
        log.info(
                "ItemServiceImpl: создание отзыва  к вещи (authorId = {}, itemId = {}, commentData = {})",
                authorId,
                itemId,
                commentData
        );

        LocalDateTime now = LocalDateTime.now();

        User author = userRepository.findById(authorId).orElseThrow(() -> {
            String message = String.format("Пользователь с id=%d не найден", authorId);
            return new NotFoundException(message);
        });

        if (!itemRepository.existsById(itemId)) {
            String message = String.format("Вещь с id=%d не найдена", itemId);
            throw new NotFoundException(message);
        }

        bookingRepository.findFirstByItemIdAndBookerIdAndStatusAndEndBefore(
                itemId,
                authorId,
                BookingStatus.APPROVED,
                now
        ).orElseThrow(() -> {
            String message = String.format("пользователь с id=%d не брал в аренду вещь с id=%d", authorId, itemId);
            return new ValidationException(message);
        });

        Comment comment = CommentMapper.mapCommentCreateRequestToComment(commentData, author, itemId, now);
        commentRepository.save(comment);

        log.info(
                "ItemServiceImpl: создан отзыв к вещи (authorId = {}, itemId = {}, commentData = {})",
                authorId,
                itemId,
                commentData
        );

        return CommentMapper.mapCommentToCommentResponse(comment, author.getName());

    }

    private LocalDateTime findLastBookingOfItem(Collection<Booking> itemBookings, LocalDateTime now) {
        return itemBookings.stream()
                .filter(booking -> booking.getEnd().isBefore(now))
                .max(Comparator.comparing(Booking::getEnd))
                .map(Booking::getStart)
                .orElse(null);
    }

    private LocalDateTime findNextBookingOfItem(Collection<Booking> itemBookings, LocalDateTime now) {
        return itemBookings.stream()
                .filter(booking -> booking.getStart().isAfter(now))
                .min(Comparator.comparing(Booking::getStart))
                .map(Booking::getStart)
                .orElse(null);
    }

    private List<CommentResponse> getCommentsForItem(Long itemId) {
        return commentRepository.findByItemIdOrderByCreatedDesc(itemId)
                .stream()
                .map(comment -> CommentMapper.mapCommentToCommentResponse(comment, comment.getAuthor().getName()))
                .toList();
    }

    private Map<Long, List<CommentResponse>> getCommentsForItems(Collection<Item> items) {
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .toList();

        if (itemIds.isEmpty()) {
            return Map.of();
        }

        return commentRepository.findByItemIdInOrderByCreatedDesc(itemIds)
                .stream()
                .map(comment -> CommentMapper.mapCommentToCommentResponse(comment, comment.getAuthor().getName()))
                .collect(Collectors.groupingBy(CommentResponse::getItemId));
    }

}
