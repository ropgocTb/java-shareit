package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.storage.Items;

import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {
    private final Items storage;

    public ItemServiceImpl(@Qualifier("InMemoryItems") Items storage) {
        this.storage = storage;
    }

    @Override
    public ItemDto addItem(Long ownerId, ItemDto itemDto) {
        return ItemMapper.toItemDto(storage.addItem(ownerId, ItemMapper.toItem(itemDto)));
    }

    @Override
    public List<ItemDto> getItems(Long ownerId) {
        return storage.getItems(ownerId).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> searchItems(String query) {
        return storage.searchItems(query).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public ItemDto getItem(Long id) {
        return ItemMapper.toItemDto(storage.getItem(id)
                .orElseThrow(() -> new NotFoundException("Вещь с таким id не найдена")));
    }

    @Override
    public ItemDto editItem(Long id, Long ownerId, ItemDto itemDto) {
        getItem(id);
        itemDto.setId(id);
        return ItemMapper.toItemDto(storage.editItem(id, ownerId, ItemMapper.toItem(itemDto)));
    }
}
