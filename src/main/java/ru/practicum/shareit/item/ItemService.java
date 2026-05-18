package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemCreateRequest;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.item.dto.ItemUpdateRequest;

import java.util.Collection;

public interface ItemService {

    ItemResponse createItem(ItemCreateRequest itemData, Long ownerId);

    ItemResponse updateItem(Long userId, Long itemId, ItemUpdateRequest itemData);

    ItemResponse getItemById(Long itemId);

    Collection<ItemResponse> getItemsByOwner(Long userId);

    Collection<ItemResponse> searchForItems(String text);

}
