package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService service;

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return service.getItems(ownerId);
    }

    @GetMapping("/{id}")
    public ItemDto getItem(@PathVariable(name = "id") Long id) {
        return service.getItem(id);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam(name = "text") String query) {
        return service.searchItems(query);
    }

    @PostMapping
    public ItemDto postItem(@RequestHeader("X-Sharer-User-Id") Long ownerId, @Valid @RequestBody ItemDto itemDto) {
        return service.addItem(ownerId, itemDto);
    }

    @PatchMapping("/{id}")
    public ItemDto patchItem(@PathVariable(name = "id") Long id,
                             @RequestHeader("X-Sharer-User-Id") Long ownerId,
                             @RequestBody ItemDto itemDto) {
        return service.editItem(id, ownerId, itemDto);
    }
}
