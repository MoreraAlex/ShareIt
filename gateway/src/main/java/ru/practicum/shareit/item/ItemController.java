package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentCreateRequest;
import ru.practicum.shareit.item.dto.ItemCreateRequest;
import ru.practicum.shareit.item.dto.ItemUpdateRequest;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody @Valid ItemCreateRequest itemData
    ) {
        log.info("Gateway ItemController: создание вещи {} (userId={})", itemData, userId);
        return itemClient.createItem(userId, itemData);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody @Valid ItemUpdateRequest itemData
    ) {
        log.info("Gateway ItemController: обновление вещи {} (itemId={}, userId={})", itemData, itemId, userId);
        return itemClient.updateItem(userId, itemId, itemData);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @PathVariable Long itemId) {
        log.info("Gateway ItemController: получение вещи (itemId={}, userId={})", itemId, userId);
        return itemClient.getItemById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsByOwner(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Gateway ItemController: получение вещей владельца (userId={})", userId);
        return itemClient.getItemsByOwner(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchForItems(@RequestParam String text) {
        log.info("Gateway ItemController: поиск вещей (text={})", text);
        return itemClient.searchForItems(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody @Valid CommentCreateRequest commentData
    ) {
        log.info("Gateway ItemController: создание комментария (userId={}, itemId={})", userId, itemId);
        return itemClient.createComment(userId, itemId, commentData);
    }
}
