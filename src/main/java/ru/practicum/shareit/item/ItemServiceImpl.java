package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemCreateRequest;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.item.dto.ItemUpdateRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserStorage;

import java.util.ArrayList;
import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public ItemResponse createItem(ItemCreateRequest itemData, Long ownerId) {
        log.info("ItemServiceImpl: начало добавления новой вещи {} пользователем {}", itemData, ownerId);
        userStorage.checkIfUserExists(ownerId);
        Item item = ItemMapper.mapItemCreateRequestToItem(itemData);
        item.setOwnerId(ownerId);
        log.info("ItemServiceImpl: добавлена новая вещь {} пользователем {}", itemData, ownerId);
        return ItemMapper.mapToItemResponse(itemStorage.addItem(item));
    }

    @Override
    public ItemResponse updateItem(Long ownerId, Long itemId, ItemUpdateRequest itemData) {
        log.info("ItemServiceImpl: начало обновления данных вещи {} (ownerId={}, itemId={})", itemData, ownerId, itemId);
        userStorage.checkIfUserExists(ownerId);
        Item item = itemStorage.getItemById(itemId);
        if (!item.getOwnerId().equals(ownerId)) {
            String message = String.format("Пользователь %d не является владельцем вещи %d", ownerId, itemId);
            throw new NotFoundException(message);
        }
        ItemMapper.updateItemFields(item, itemData);
        itemStorage.updateItem(item);
        log.info("ItemServiceImpl: обновлены данные вещи {} (ownerId={}, itemId={})", itemData, ownerId, itemId);
        return ItemMapper.mapToItemResponse(item);
    }

    @Override
    public ItemResponse getItemById(Long itemId) {
        log.info("ItemServiceImpl: получение данных вещи по id (itemId={})", itemId);
        return ItemMapper.mapToItemResponse(itemStorage.getItemById(itemId));
    }

    @Override
    public Collection<ItemResponse> getItemsByOwner(Long userId) {
        log.info("ItemServiceImpl: получение вещей пользователя (userId={})", userId);
        return itemStorage.getItemsByOwner(userId)
                .stream()
                .map(ItemMapper::mapToItemResponse).toList();
    }

    @Override
    public Collection<ItemResponse> searchForItems(String text) {
        log.info("ItemServiceImpl: поиск вещей по строке (text={})", text);
        if (text.isEmpty()) {
            return new ArrayList<>();
        }
        return itemStorage.searchForItems(text).stream()
                .map(ItemMapper::mapToItemResponse)
                .toList();
    }

}
