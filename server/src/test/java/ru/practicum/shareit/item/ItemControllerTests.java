package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsDto;
import ru.practicum.shareit.item.service.ItemServiceDb;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
//тесты эндпойнтов с моками в слое сервисов
public class ItemControllerTests {
    private MockMvc mvc;
    private ObjectMapper objectMapper;

    @Mock
    private ItemServiceDb itemServiceDb;

    @InjectMocks
    private ItemController itemController;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(itemController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getItemsShouldReturnItemWithCommentsDtoList() throws Exception {
        UserDto userDto = UserDto.builder()
                .name("test_user")
                .email("test@gmail.com")
                .build();

        ItemWithCommentsDto itemDto = ItemWithCommentsDto.builder()
                .id(1L)
                .name("test_name")
                .description("test_desc")
                .available(true)
                .owner(userDto)
                .build();
        ItemWithCommentsDto itemDto1 = ItemWithCommentsDto.builder()
                .id(2L)
                .name("test2_name")
                .description("test2_desc")
                .available(true)
                .owner(userDto)
                .build();

        when(itemServiceDb.getItems(anyLong())).thenReturn(List.of(itemDto, itemDto1));

        mvc.perform(get("/items")
                .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(itemDto1.getId()), Long.class));

        verify(itemServiceDb, times(1)).getItems(anyLong());
    }

    @Test
    void getItemShouldReturnItemWithCommentsDtoList() throws Exception {
        UserDto userDto = UserDto.builder()
                .name("test_user")
                .email("test@gmail.com")
                .build();

        ItemWithCommentsDto itemDto = ItemWithCommentsDto.builder()
                .id(1L)
                .name("test_name")
                .description("test_desc")
                .available(true)
                .owner(userDto)
                .build();
        when(itemServiceDb.getItem(anyLong())).thenReturn(itemDto);

        mvc.perform(get("/items/{id}", anyLong()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())));

        verify(itemServiceDb, times(1)).getItem(anyLong());
    }

    @Test
    void searchItemsShouldReturnItemDtoList() throws Exception {
        UserDto userDto = UserDto.builder()
                .name("test_user")
                .email("test@gmail.com")
                .build();

        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("test_name")
                .description("test_desc")
                .available(true)
                .owner(userDto)
                .build();

        ItemDto itemDto1 = ItemDto.builder()
                .id(2L)
                .name("test1_name")
                .description("test1_desc")
                .available(true)
                .owner(userDto)
                .build();

        when(itemServiceDb.searchItems(anyString())).thenReturn(List.of(itemDto, itemDto1));

        mvc.perform(get("/items/search?text={text}", anyString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(itemDto1.getId()), Long.class));

        verify(itemServiceDb, times(1)).searchItems(anyString());
    }

    @Test
    void postItemShouldReturnItemDto() throws Exception {
        UserDto userDto = UserDto.builder()
                .name("test_user")
                .email("test@gmail.com")
                .build();

        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("test_name")
                .description("test_desc")
                .available(true)
                .owner(userDto)
                .build();

        when(itemServiceDb.addItem(anyLong(), any(ItemDto.class))).thenReturn(itemDto);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())));

        verify(itemServiceDb, times(1)).addItem(anyLong(), any(ItemDto.class));
    }

    @Test
    void postCommentShouldReturnCommentDto() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("test_user")
                .email("test@gmail.com")
                .build();

        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("asdfglasdjfalsdf")
                .author(userDto)
                .build();

        when(itemServiceDb.addComment(anyLong(), anyLong(), anyString())).thenReturn(commentDto);

        mvc.perform(post("/items/{id}/comment", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.author.id", is(commentDto.getAuthor().getId()), Long.class));

        verify(itemServiceDb, times(1)).addComment(anyLong(), anyLong(), anyString());
    }

    @Test
    void patchItemShouldReturnItemDto() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("test_user")
                .email("test@gmail.com")
                .build();

        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("test_name")
                .description("test_desc")
                .available(true)
                .owner(userDto)
                .build();

        when(itemServiceDb.editItem(anyLong(), anyLong(), any(ItemDto.class))).thenReturn(itemDto);

        mvc.perform(patch("/items/{id}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.owner.id", is(itemDto.getOwner().getId()), Long.class));

        verify(itemServiceDb, times(1)).editItem(anyLong(), anyLong(), any(ItemDto.class));

    }
}
