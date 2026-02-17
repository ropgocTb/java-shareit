package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto addRequest(Long userId, ItemRequestDto itemRequestDto);

    ItemRequestDto getRequest(Long id);

    List<ItemRequestDto> getRequests();

    List<ItemRequestDto> getUserRequests(Long userId);
}
