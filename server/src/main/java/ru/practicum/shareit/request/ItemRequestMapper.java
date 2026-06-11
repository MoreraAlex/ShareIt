package ru.practicum.shareit.request;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseItemDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

@UtilityClass
public class ItemRequestMapper {

    public ItemRequest mapToItemRequest(ItemRequestDto itemRequestDto, User requester, LocalDateTime created) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestDto.getDescription());
        itemRequest.setRequester(requester);
        itemRequest.setCreated(created);
        return itemRequest;
    }

    public ItemRequest mapToItemRequest(ItemRequestCreateDto itemRequestDto, User requester, LocalDateTime created) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestDto.getDescription());
        itemRequest.setRequester(requester);
        itemRequest.setCreated(created);
        return itemRequest;
    }

    public ItemRequestDto mapToItemRequestDto(ItemRequest itemRequest, List<ItemRequestResponseItemDto> items) {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(itemRequest.getId());
        itemRequestDto.setDescription(itemRequest.getDescription());
        itemRequestDto.setCreated(itemRequest.getCreated());
        itemRequestDto.setItems(items);
        return itemRequestDto;
    }

    public ItemRequestResponseDto mapToItemRequestResponseDto(
            ItemRequest itemRequest,
            List<ItemRequestResponseItemDto> items
    ) {
        ItemRequestResponseDto responseDto = new ItemRequestResponseDto();
        responseDto.setId(itemRequest.getId());
        responseDto.setDescription(itemRequest.getDescription());
        responseDto.setCreated(itemRequest.getCreated());
        responseDto.setItems(items);
        return responseDto;
    }

    public ItemRequestResponseItemDto mapToResponseItemDto(Item item) {
        ItemRequestResponseItemDto responseItemDto = new ItemRequestResponseItemDto();
        responseItemDto.setId(item.getId());
        responseItemDto.setName(item.getName());
        responseItemDto.setOwnerId(item.getOwner().getId());
        return responseItemDto;
    }
}
