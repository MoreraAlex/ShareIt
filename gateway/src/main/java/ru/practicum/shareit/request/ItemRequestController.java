package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createRequest(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody @Valid ItemRequestDto itemRequestDto
    ) {
        log.info("Gateway ItemRequestController: создание запроса вещи (userId={}, data={})", userId, itemRequestDto);
        return itemRequestClient.createRequest(userId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Gateway ItemRequestController: получение своих запросов (userId={})", userId);
        return itemRequestClient.getOwnRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        validatePagination(from, size);
        log.info("Gateway ItemRequestController: получение чужих запросов (userId={}, from={}, size={})",
                userId, from, size);
        return itemRequestClient.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long requestId
    ) {
        log.info("Gateway ItemRequestController: получение запроса (userId={}, requestId={})", userId, requestId);
        return itemRequestClient.getRequestById(userId, requestId);
    }

    private void validatePagination(Integer from, Integer size) {
        if (from < 0) {
            throw new IllegalArgumentException("from не может быть меньше 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size должен быть больше 0");
        }
    }
}
