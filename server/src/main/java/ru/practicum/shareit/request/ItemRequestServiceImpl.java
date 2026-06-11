package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.pagination.OffsetPageRequest;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto createRequest(Long requesterId, ItemRequestDto itemRequestDto) {
        log.info("ItemRequestServiceImpl: создание запроса вещи пользователем {}", requesterId);
        User requester = userRepository.findById(requesterId).orElseThrow(() -> {
            String message = String.format("Пользователь с id=%d не найден", requesterId);
            return new NotFoundException(message);
        });

        ItemRequest itemRequest = ItemRequestMapper.mapToItemRequest(itemRequestDto, requester, LocalDateTime.now());
        itemRequest = itemRequestRepository.save(itemRequest);
        return ItemRequestMapper.mapToItemRequestDto(itemRequest, List.of());
    }

    @Override
    public Collection<ItemRequestDto> getOwnRequests(Long requesterId) {
        log.info("ItemRequestServiceImpl: получение запросов пользователя {}", requesterId);
        checkUserExists(requesterId);

        Sort newestFirst = Sort.by(Sort.Direction.DESC, "created");
        List<ItemRequest> requests = itemRequestRepository.findAllByRequesterId(requesterId, newestFirst);
        return mapRequestsWithItems(requests);
    }

    @Override
    public Collection<ItemRequestDto> getAllRequests(Long requesterId, Integer from, Integer size) {
        log.info("ItemRequestServiceImpl: получение запросов других пользователей (requesterId={}, from={}, size={})",
                requesterId, from, size);
        checkUserExists(requesterId);

        Sort newestFirst = Sort.by(Sort.Direction.DESC, "created");
        OffsetPageRequest pageRequest = new OffsetPageRequest(from, size, newestFirst);
        List<ItemRequest> requests = itemRequestRepository.findAllByRequesterIdNot(requesterId, pageRequest).toList();
        return mapRequestsWithItems(requests);
    }

    @Override
    public ItemRequestDto getRequestById(Long requesterId, Long requestId) {
        log.info("ItemRequestServiceImpl: получение запроса {} пользователем {}", requestId, requesterId);
        checkUserExists(requesterId);

        ItemRequest itemRequest = itemRequestRepository.findById(requestId).orElseThrow(() -> {
            String message = String.format("Запрос вещи с id=%d не найден", requestId);
            return new NotFoundException(message);
        });
        List<ItemRequestResponseItemDto> items = itemRepository.findAllByRequestId(requestId)
                .stream()
                .map(ItemRequestMapper::mapToResponseItemDto)
                .toList();
        return ItemRequestMapper.mapToItemRequestDto(itemRequest, items);
    }

    @Override
    @Transactional
    public ItemRequestResponseDto createItemRequest(Long requesterId, ItemRequestCreateDto itemRequestDto) {
        log.info("ItemRequestServiceImpl: создание запроса вещи пользователем {}", requesterId);
        User requester = userRepository.findById(requesterId).orElseThrow(() -> {
            String message = String.format("Пользователь с id=%d не найден", requesterId);
            return new NotFoundException(message);
        });

        ItemRequest itemRequest = ItemRequestMapper.mapToItemRequest(itemRequestDto, requester, LocalDateTime.now());
        itemRequest = itemRequestRepository.save(itemRequest);
        return ItemRequestMapper.mapToItemRequestResponseDto(itemRequest, List.of());
    }

    @Override
    public Collection<ItemRequestResponseDto> getItemRequestsOfUser(Long requesterId) {
        checkUserExists(requesterId);

        Sort newestFirst = Sort.by(Sort.Direction.DESC, "created");
        List<ItemRequest> requests = itemRequestRepository.findAllByRequesterId(requesterId, newestFirst);
        return mapRequestsWithResponseItems(requests);
    }

    @Override
    public Collection<ItemRequestResponseDto> getAllItemRequests(Long requesterId) {
        return getAllItemRequests(requesterId, 0, 10);
    }

    @Override
    public Collection<ItemRequestResponseDto> getAllItemRequests(Long requesterId, Integer from, Integer size) {
        checkUserExists(requesterId);

        Sort newestFirst = Sort.by(Sort.Direction.DESC, "created");
        OffsetPageRequest pageRequest = new OffsetPageRequest(from, size, newestFirst);
        List<ItemRequest> requests = itemRequestRepository.findAllByRequesterIdNot(requesterId, pageRequest).toList();
        return mapRequestsWithResponseItems(requests);
    }

    @Override
    public ItemRequestResponseDto getItemRequestById(Long requesterId, Long requestId) {
        checkUserExists(requesterId);

        ItemRequest itemRequest = itemRequestRepository.findById(requestId).orElseThrow(() -> {
            String message = String.format("Запрос вещи с id=%d не найден", requestId);
            return new NotFoundException(message);
        });
        List<ItemRequestResponseItemDto> items = itemRepository.findAllByRequestId(requestId)
                .stream()
                .map(ItemRequestMapper::mapToResponseItemDto)
                .toList();
        return ItemRequestMapper.mapToItemRequestResponseDto(itemRequest, items);
    }

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            String message = String.format("Пользователь с id=%d не найден", userId);
            throw new NotFoundException(message);
        }
    }

    private Collection<ItemRequestDto> mapRequestsWithItems(List<ItemRequest> requests) {
        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        Map<Long, List<ItemRequestResponseItemDto>> itemsByRequestId = requestIds.isEmpty()
                ? Map.of()
                : itemRepository.findAllByRequestIdIn(requestIds)
                .stream()
                .collect(Collectors.groupingBy(
                        item -> item.getRequest().getId(),
                        Collectors.mapping(ItemRequestMapper::mapToResponseItemDto, Collectors.toList())
                ));

        return requests.stream()
                .map(request -> ItemRequestMapper.mapToItemRequestDto(
                        request,
                        itemsByRequestId.getOrDefault(request.getId(), List.of())
                ))
                .toList();
    }

    private Collection<ItemRequestResponseDto> mapRequestsWithResponseItems(List<ItemRequest> requests) {
        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        Map<Long, List<ItemRequestResponseItemDto>> itemsByRequestId = requestIds.isEmpty()
                ? Map.of()
                : itemRepository.findAllByRequestIdIn(requestIds)
                .stream()
                .collect(Collectors.groupingBy(
                        item -> item.getRequest().getId(),
                        Collectors.mapping(ItemRequestMapper::mapToResponseItemDto, Collectors.toList())
                ));

        return requests.stream()
                .map(request -> ItemRequestMapper.mapToItemRequestResponseDto(
                        request,
                        itemsByRequestId.getOrDefault(request.getId(), List.of())
                ))
                .toList();
    }
}
