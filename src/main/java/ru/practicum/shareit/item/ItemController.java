package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.ItemCreateRequest;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.item.dto.ItemUpdateRequest;

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
    public ItemResponse getItemById(@PathVariable Long itemId) {
        log.info("ItemController: получен запрос на получение данных вещи (itemId={})", itemId);
        return itemService.getItemById(itemId);
    }

    @GetMapping
    public Collection<ItemResponse> getItemsByOwner(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("ItemController: получен запрос на получение вещей владельца (userId={})", userId);
        return itemService.getItemsByOwner(userId);
    }

    @GetMapping("/search")
    public Collection<ItemResponse> searchForItems(@RequestParam String text) {
        log.info("ItemController: получен запрос на поиск вещей по строке (text={})", text);
        return itemService.searchForItems(text);
    }
}
