package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService service;

    public ItemController(@Qualifier("ItemServiceDb") ItemService service) {
        this.service = service;
    }

    @GetMapping
    public List<ItemWithCommentsDto> getItems(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return service.getItems(ownerId);
    }

    @GetMapping("/{id}")
    public ItemWithCommentsDto getItem(@PathVariable(name = "id") Long id) {
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

    @PostMapping("/{itemId}/comment")
    public CommentDto postComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                  @PathVariable Long itemId, @Valid @RequestBody CommentDto commentDto) {
        return service.addComment(userId, itemId, commentDto.getText());
    }

    @PatchMapping("/{id}")
    public ItemDto patchItem(@PathVariable(name = "id") Long id,
                             @RequestHeader("X-Sharer-User-Id") Long ownerId,
                             @RequestBody ItemDto itemDto) {
        return service.editItem(id, ownerId, itemDto);
    }
}
