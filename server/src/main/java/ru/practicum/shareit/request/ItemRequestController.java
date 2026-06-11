package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.Collection;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestResponseDto createRequest(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody @Valid ItemRequestCreateDto itemRequestDto
    ) {
        log.info("ItemRequestController: получен запрос на создание запроса вещи (userId={}, data={})",
                userId, itemRequestDto);
        return itemRequestService.createItemRequest(userId, itemRequestDto);
    }

    @GetMapping
    public Collection<ItemRequestResponseDto> getOwnRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("ItemRequestController: получен запрос на получение своих запросов (userId={})", userId);
        return itemRequestService.getItemRequestsOfUser(userId);
    }

    @GetMapping("/all")
    public Collection<ItemRequestResponseDto> getAllRequests(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("ItemRequestController: получен запрос на получение чужих запросов (userId={}, from={}, size={})",
                userId, from, size);
        return itemRequestService.getAllItemRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestResponseDto getRequestById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long requestId
    ) {
        log.info("ItemRequestController: получен запрос на получение запроса (userId={}, requestId={})",
                userId, requestId);
        return itemRequestService.getItemRequestById(userId, requestId);
    }
}
