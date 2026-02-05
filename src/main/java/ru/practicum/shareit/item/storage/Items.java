package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface Items {
    Item addItem(Long ownerId, Item item);

    List<Item> getItems(Long ownerId);

    Optional<Item> getItem(Long id);

    Item editItem(Long id, Long ownerId, Item item);

    List<Item> searchItems(String query);
}
