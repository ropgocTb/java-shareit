package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService service;

    public ItemRequestController(ItemRequestService service) {
        this.service = service;
    }

    //получить список своих запросов вместе с данными об ответах на них
    @GetMapping
    public List<ItemRequestDto> getRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return service.getUserRequests(userId);
    }

    //получить список запросов созданных другими пользователями
    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return service.getRequests(userId);
    }

    //получить данные о конкретном запросе вместе с данными об ответах на него
    @GetMapping("/{id}")
    public ItemRequestDto getRequest(@PathVariable(name = "id") Long id) {
        return service.getRequest(id);
    }

    //сделать новый запрос вещи
    @PostMapping
    public ItemRequestDto postRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                      @Valid @RequestBody ItemRequestDto itemRequestDto) {
        return service.addRequest(userId, itemRequestDto);
    }
}
