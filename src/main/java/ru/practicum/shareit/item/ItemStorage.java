package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemStorage {

    Item addItem(Item item);

    Item updateItem(Item item);

    Item getItemById(Long itemId);

    Collection<Item> getItemsByOwner(Long userId);

    Collection<Item> searchForItems(String text);

}
