package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    private Long id;

    @NotBlank
    private String text;

    private ItemDto item;
    //это поле требуется для тестов Postman
    private String authorName;
    private UserDto author;
    private LocalDateTime created;
}
