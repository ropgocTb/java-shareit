package ru.practicum.shareit.request.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service("ItemRequestServiceDb")
@Transactional(readOnly = true)
public class ItemRequestServiceDb implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ItemRequestServiceDb(ItemRequestRepository itemRequestRepository,
                                ItemRepository itemRepository,
                                UserRepository userRepository) {
        this.itemRequestRepository = itemRequestRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = false)
    @Override
    public ItemRequestDto addRequest(Long userId, ItemRequestDto itemRequestDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest itemRequest = ItemRequest.builder()
                .requestor(user)
                .description(itemRequestDto.getDescription())
                .created(LocalDateTime.now())
                .build();

        return ItemRequestMapper.toItemRequestDto(itemRequestRepository.save(itemRequest), List.of());
    }

    @Override
    public ItemRequestDto getRequest(Long id) {
        ItemRequest itemRequest = itemRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Запрос с таким id не найден"));

        List<Item> answersForRequest = itemRepository.findAllByRequest_Id(id);

        if (answersForRequest.isEmpty())
            return ItemRequestMapper.toItemRequestDto(itemRequest, List.of());

        return ItemRequestMapper.toItemRequestDto(itemRequest, answersForRequest.stream()
                .map(ItemMapper::toItemDto)
                .toList());
    }

    @Override
    public List<ItemRequestDto> getRequests() {
        List<ItemRequestDto> itemRequests = new ArrayList<>();

        Sort sortByCreated = Sort.by(Sort.Direction.DESC, "created");
        Pageable page = PageRequest.of(0, 15, sortByCreated);

        do {
            Page<ItemRequest> itemRequestPage = itemRequestRepository.findAll(page);

            itemRequests.addAll(itemRequestPage.getContent().stream()
                    .map(itemRequest -> {
                        List<Item> answersForRequest = itemRepository.findAllByRequest_Id(itemRequest.getId());

                        if (answersForRequest.isEmpty())
                            return ItemRequestMapper.toItemRequestDto(itemRequest, List.of());

                        return ItemRequestMapper.toItemRequestDto(itemRequest, answersForRequest.stream()
                                .map(ItemMapper::toItemDto)
                                .toList());
                    })
                    .toList());

            if (itemRequestPage.hasNext()) {
                page = itemRequestPage.nextOrLastPageable();
            } else {
                page = null;
            }
        } while (page != null);

        return itemRequests;
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        List<ItemRequest> requests = itemRequestRepository.findByRequestor_IdOrderByCreatedDesc(userId);

        if (requests.isEmpty())
            return List.of();

        return requests.stream()
                .map(itemRequest -> {
                    List<Item> answersForRequest = itemRepository.findAllByRequest_Id(itemRequest.getId());

                    if (answersForRequest.isEmpty())
                        return ItemRequestMapper.toItemRequestDto(itemRequest, List.of());

                    return ItemRequestMapper.toItemRequestDto(itemRequest, answersForRequest.stream()
                            .map(ItemMapper::toItemDto)
                            .toList());
                })
                .toList();
    }
}
