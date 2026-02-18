package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @GetMapping
    public ResponseEntity<Object> getItems(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemClient.getItems(ownerId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getItem(@PathVariable(name = "id") Long id) {
        return itemClient.getItem(id);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam(name = "text") String query) {
        return itemClient.searchItems(query);
    }

    @PostMapping
    public ResponseEntity<Object> postItem(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                           @Valid @RequestBody ItemDto itemDto) {
        return itemClient.addItem(ownerId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> postComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @PathVariable(name = "itemId") Long itemId,
                                              @Valid @RequestBody CommentDto commentDto) {
        return itemClient.addComment(userId, itemId, commentDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> patchItem(@PathVariable(name = "id") Long id,
                                            @RequestHeader("X-Sharer-User-Id") Long ownerId,
                                            @RequestBody ItemDto itemDto) {
        return itemClient.editItem(id, ownerId, itemDto);
    }

}
