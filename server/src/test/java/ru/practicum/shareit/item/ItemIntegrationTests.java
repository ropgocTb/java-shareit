package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ItemIntegrationTests {
    @Autowired
    @Qualifier("ItemServiceDb")
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    private Item item;
    private User user;
    private User bookerCommentator;
    private ItemDto itemDto;
    private Booking lastBooking;
    private Booking nextBooking;
    private Comment comment;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        user = User.builder()
                .name("test_user")
                .email("test_user@gmail.com")
                .build();

        userRepository.save(user);

        bookerCommentator = User.builder()
                .name("bookerCommentator")
                .email("bookercommentator@gmail.com")
                .build();

        item = Item.builder()
                .name("item_test")
                .description("item_desc")
                .owner(user)
                .available(true)
                .build();

        itemDto = ItemDto.builder()
                .name("item_dto")
                .description("item_dto_desc")
                .owner(UserMapper.toUserDto(user))
                .available(true)
                .build();
    }

    @Test
    public void getItemWithBookingsAndCommentsTest() throws Exception {
        Item savedItem = itemRepository.save(ItemMapper.toItem(itemDto));

        //проверка владельцем с пустыми комментариями и бронированиями
        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$[0].owner").exists())
                .andExpect(jsonPath("$[0].lastBooking").exists())
                .andExpect(jsonPath("$[0].nextBooking").exists())
                .andExpect(jsonPath("$[0].comments").exists());

        bookerCommentator = userRepository.save(bookerCommentator);

        lastBooking = Booking.builder()
                .item(savedItem)
                .booker(bookerCommentator)
                .start(LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.SECONDS))
                .end(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .status(Status.APPROVED)
                .build();
        nextBooking = Booking.builder()
                .item(savedItem)
                .booker(bookerCommentator)
                .start(LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS))
                .end(LocalDateTime.now().plusDays(3).truncatedTo(ChronoUnit.SECONDS))
                .status(Status.WAITING)
                .build();
        comment = Comment.builder()
                .text("норм вещь кросава")
                .build();

        Booking savedLastBooking = bookingRepository.save(lastBooking);
        Booking savedNextBooking = bookingRepository.save(nextBooking);

        mvc.perform(post("/items/{id}/comment", savedItem.getId())
                        .content(objectMapper.writeValueAsString(comment))
                        .header("X-Sharer-User-Id", bookerCommentator.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text", is(comment.getText())));

        //проверка получения вещи любым пользователем
        mvc.perform(get("/items/{id}", savedItem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.owner").exists())
                .andExpect(jsonPath("$.comments.length()", is(1)));

        //проверка владельцем
        mvc.perform(get("/items")
                    .header("X-Sharer-User-Id", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$[0].owner").exists())
                .andExpect(jsonPath("$[0].lastBooking.id", is(savedLastBooking.getId()), Long.class))
                .andExpect(jsonPath("$[0].nextBooking.id", is(savedNextBooking.getId()), Long.class))
                .andExpect(jsonPath("$[0].comments.length()", is(1)));
    }

    @Test
    public void addItemTest() throws Exception {
        mvc.perform(post("/items")
                        .content(objectMapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", user.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.owner").exists());
    }

    @Test
    public void editItemTest() throws Exception {
        itemDto.setName("updated Name");
        itemDto.setDescription("updated Description");
        itemDto.setAvailable(false);

        Item savedItem = itemRepository.save(item);

        //обновление не владельцем
        mvc.perform(patch("/items/{id}", savedItem.getId())
                        .content(objectMapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", user.getId() + 29)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        mvc.perform(patch("/items/{id}", savedItem.getId())
                        .content(objectMapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", user.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.owner").exists());
    }

    @Test
    public void searchItemTest() throws Exception {
        Item savedItem = itemRepository.save(ItemMapper.toItem(itemDto));

        mvc.perform(get("/items/search?text={text}", itemDto.getName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(savedItem.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$[0].owner").exists());
    }
}
