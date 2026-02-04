package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(Long ownerId, ItemDto itemDto);

    List<ItemDto> getItems(Long ownerId);

    List<ItemDto> searchItems(String query);

    ItemDto getItem(Long id);

    ItemDto editItem(Long id, Long ownerId, ItemDto itemDto);
}
