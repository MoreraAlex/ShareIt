package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;

import java.util.Collection;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemResponse createItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody @Valid ItemCreateRequest itemData
    ) {
        log.info("ItemController: получен запрос на создание вещи {} (userId={})", itemData, userId);
        return itemService.createItem(itemData, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemResponse updateItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody @Valid ItemUpdateRequest itemData
    ) {
        log.info("ItemController: получен запрос на обновление данных вещи {} (itemId={}, userId={})", itemData, itemId, userId);
        return itemService.updateItem(userId, itemId, itemData);
    }

    @GetMapping("/{itemId}")
    public ItemResponseFull getItemById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId) {
        log.info("ItemController: получен запрос на получение данных вещи (itemId={})", itemId);
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping
    public Collection<ItemResponseFull> getItemsByOwner(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("ItemController: получен запрос на получение вещей владельца (userId={})", userId);
        return itemService.getItemsByOwner(userId);
    }

    @GetMapping("/search")
    public Collection<ItemResponse> searchForItems(@RequestParam String text) {
        log.info("ItemController: получен запрос на поиск вещей по строке (text={})", text);
        return itemService.searchForItems(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponse createComment(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody @Valid CommentCreateRequest commentData
    ) {
        log.info(
                "ItemController: получен запрос на создание отзыва (userId = {}, itemId = {}, commentData = {})",
                userId,
                itemId,
                commentData
        );
        return itemService.createComment(userId, itemId, commentData);
    }
}
