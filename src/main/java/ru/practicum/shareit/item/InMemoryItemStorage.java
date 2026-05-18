package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryItemStorage implements ItemStorage {

    private final Map<Long, Item> items = new HashMap<>();
    private Long lastId = 0L;

    @Override
    public Item addItem(Item item) {
        lastId++;
        items.put(lastId, item);
        item.setId(lastId);
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item getItemById(Long itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            String message = String.format("вещь с id=%d не найдена", itemId);
            throw new NotFoundException(message);
        }
        return item;
    }

    @Override
    public Collection<Item> getItemsByOwner(Long userId) {
        return items.values().stream()
                .filter(item -> item.getOwnerId().equals(userId))
                .toList();
    }

    @Override
    public Collection<Item> searchForItems(String text) {
        String textUpperCase = text.toUpperCase();
        return items.values().stream()
                .filter(
                        item -> item.getAvailable() &&
                            (item.getName().toUpperCase().contains(textUpperCase) ||
                                    item.getDescription().toUpperCase().contains(textUpperCase))
                )
                .toList();
    }

}
