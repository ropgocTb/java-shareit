package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(Long ownerId, ItemDto itemDto);

    List<ItemWithCommentsDto> getItems(Long ownerId);

    List<ItemDto> searchItems(String query);

    ItemWithCommentsDto getItem(Long id);

    ItemDto editItem(Long id, Long ownerId, ItemDto itemDto);

    CommentDto addComment(Long userId, Long itemId, String text);
}
