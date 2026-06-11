package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.*;

import java.util.Collection;

public interface ItemService {

    ItemResponse createItem(ItemCreateRequest itemData, Long ownerId);

    ItemResponse updateItem(Long userId, Long itemId, ItemUpdateRequest itemData);

    ItemResponseFull getItemById(Long userId, Long itemId);

    Collection<ItemResponseFull> getItemsByOwner(Long userId);

    Collection<ItemResponse> searchForItems(String text);

    CommentResponse createComment(Long authorId, Long itemId, CommentCreateRequest commentData);

}
