package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.AccessException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.storage.Users;

import java.util.*;

@Component("InMemoryItems")
public class InMemoryItems implements Items {
    private final Map<Long, Item> items = new HashMap<>();
    private final Users userStorage;

    public InMemoryItems(Users userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public Item addItem(Long ownerId, Item item) {
        item.setId(getNextId());
        item.setOwnerId(ownerId);
        validateItem(item);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> getItem(Long id) {
        return Optional.of(items.get(id));
    }

    @Override
    public List<Item> getItems(Long ownerId) {
        List<Item> ownerItems = new ArrayList<>();

        for (Item item : items.values()) {
            if (item.getOwnerId().equals(ownerId))
                ownerItems.add(item);
        }

        return ownerItems;
    }

    @Override
    public Item editItem(Long id, Long ownerId, Item item) {
        Item editedItem = items.get(id);

        if (!editedItem.getOwnerId().equals(ownerId)) {
            throw new AccessException("Изменять вещь может только владелец");
        }
        if (item.getDescription() != null) {
            editedItem.setDescription(item.getDescription());
        }
        if (item.getName() != null) {
            editedItem.setName(item.getName());
        }
        if (item.getAvailable() != null && item.getAvailable() != editedItem.getAvailable()) {
            editedItem.setAvailable(item.getAvailable());
        }

        validateItem(editedItem);
        items.put(id, editedItem);

        return editedItem;
    }

    @Override
    public List<Item> searchItems(String query) {
        List<Item> searchedItems = new ArrayList<>();

        if (query.isBlank())
            return searchedItems;

        for (Item item : items.values()) {
            if ((item.getName().toLowerCase().contains(query.toLowerCase())
                    || item.getDescription().toLowerCase().contains(query.toLowerCase()))
                    && item.getAvailable().equals(true))
                searchedItems.add(item);
        }

        return searchedItems;
    }

    private void validateItem(Item item) {
        if (userStorage.getUser(item.getOwnerId()).isEmpty()) {
            throw new NotFoundException("Пользователь с таким id не найден");
        }
        if (item.getName() == null || item.getName().isBlank()) {
            throw new ValidationException("Имя вещи не может быть пустым");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new ValidationException("Описание вещи не может быть пустым");
        }
    }

    private long getNextId() {
        long currentMaxId = items.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
