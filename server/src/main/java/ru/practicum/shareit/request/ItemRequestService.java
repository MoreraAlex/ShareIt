package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.Collection;

public interface ItemRequestService {

    ItemRequestDto createRequest(Long requesterId, ItemRequestDto itemRequestDto);

    Collection<ItemRequestDto> getOwnRequests(Long requesterId);

    Collection<ItemRequestDto> getAllRequests(Long requesterId, Integer from, Integer size);

    ItemRequestDto getRequestById(Long requesterId, Long requestId);

    ItemRequestResponseDto createItemRequest(Long requesterId, ItemRequestCreateDto itemRequestDto);

    Collection<ItemRequestResponseDto> getItemRequestsOfUser(Long requesterId);

    Collection<ItemRequestResponseDto> getAllItemRequests(Long requesterId);

    Collection<ItemRequestResponseDto> getAllItemRequests(Long requesterId, Integer from, Integer size);

    ItemRequestResponseDto getItemRequestById(Long requesterId, Long requestId);
}
