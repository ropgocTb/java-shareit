package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class RequestIntegrationTests {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    private ItemRequestDto itemRequestDto;
    private User savedUser;
    private ItemRequest savedItemRequest;

    @BeforeEach
    void setUp() {
        itemRequestRepository.deleteAll();
        itemRepository.deleteAll();
        itemRequestRepository.deleteAll();

        User user = User.builder()
                .name("test_user")
                .email("test_user@gmail.com")
                .build();

        savedUser = userRepository.save(user);

        User userSupplier = User.builder()
                .name("supplier")
                .email("supplier@gmail.com")
                .build();

        User savedUserSupplier = userRepository.save(userSupplier);

        itemRequestDto = ItemRequestDto.builder()
                .description("надо вот такая вот штука")
                .build();

        ItemRequest itemRequest = ItemRequest.builder()
                .description("asdfasdf")
                .requestor(savedUser)
                .created(LocalDateTime.now())
                .build();

        savedItemRequest = itemRequestRepository.save(itemRequest);

        Item item = Item.builder()
                .name("вот та штука которая надо")
                .description("точно она проверяй")
                .owner(userSupplier)
                .available(true)
                .request(savedItemRequest)
                .build();

        Item savedItemOnRequest = itemRepository.save(item);
    }

    @Test
    public void addAndGetRequestTest() throws Exception {
        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", savedUser.getId())
                        .content(objectMapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.requestorId", is(savedUser.getId()), Long.class))
                .andExpect(jsonPath("$.created").exists());

        mvc.perform(get("/requests/{id}", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2L), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.requestorId", is(savedUser.getId()), Long.class))
                .andExpect(jsonPath("$.created").exists());

        //показывает порядок сортировки - от более новых к более старым(последний запрос добавился в начало
        // возвращаемого списка)
        mvc.perform(get("/requests")
                    .header("X-Sharer-User-Id", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].id", is(2L), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$[0].requestorId", is(savedUser.getId()), Long.class))
                .andExpect(jsonPath("$[0].created").exists());

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].id", is(2L), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$[0].requestorId", is(savedUser.getId()), Long.class))
                .andExpect(jsonPath("$[0].created").exists());
    }

    @Test
    public void getRequestsWithAnswersTest() throws Exception {
        mvc.perform(get("/requests/{id}", savedItemRequest.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.description", is(savedItemRequest.getDescription())))
                .andExpect(jsonPath("$.requestorId", is(savedUser.getId()), Long.class))
                .andExpect(jsonPath("$.created").exists())
                .andExpect(jsonPath("$.items.length()", is(1)));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].description", is(savedItemRequest.getDescription())))
                .andExpect(jsonPath("$[0].requestorId", is(savedUser.getId()), Long.class))
                .andExpect(jsonPath("$[0].created").exists())
                .andExpect(jsonPath("$[0].items.length()", is(1)));

        //пустой список для любых пользователей без запросов
        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", savedUser.getId() + 29))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));

    }
}
