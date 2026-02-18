package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestServiceDb;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
//тесты эндпойнтов с моками в слое сервисов
public class ItemRequestControllerTests {
    private MockMvc mvc;
    private ObjectMapper objectMapper;

    @Mock
    private ItemRequestServiceDb itemRequestServiceDb;

    @InjectMocks
    private ItemRequestController itemRequestController;


    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(itemRequestController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getUserRequestsTestShouldReturnList() throws Exception {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("test_desc")
                .requestorId(1L)
                .build();

        ItemRequestDto itemRequestDto1 = ItemRequestDto.builder()
                .id(2L)
                .description("test1_desc")
                .requestorId(1L)
                .build();

        when(itemRequestServiceDb.getUserRequests(anyLong())).thenReturn(List.of(itemRequestDto, itemRequestDto1));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(itemRequestDto1.getId()), Long.class));

        verify(itemRequestServiceDb, times(1)).getUserRequests(anyLong());
    }

    @Test
    void getAllRequestsTestShouldReturnList() throws Exception  {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("test_desc")
                .requestorId(1L)
                .build();

        ItemRequestDto itemRequestDto1 = ItemRequestDto.builder()
                .id(2L)
                .description("test1_desc")
                .requestorId(1L)
                .build();

        when(itemRequestServiceDb.getRequests(anyLong())).thenReturn(List.of(itemRequestDto, itemRequestDto1));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(itemRequestDto1.getId()), Long.class));

        verify(itemRequestServiceDb, times(1)).getRequests(anyLong());
    }

    @Test
    void getRequestTestShouldReturnItemRequestDto() throws Exception  {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("test_desc")
                .requestorId(1L)
                .build();

        when(itemRequestServiceDb.getRequest(anyLong())).thenReturn(itemRequestDto);

        mvc.perform(get("/requests/{id}", anyLong()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.requestorId", is(itemRequestDto.getRequestorId()), Long.class));

        verify(itemRequestServiceDb, times(1)).getRequest(anyLong());

    }

    @Test
    void postRequestTestShouldReturnItemRequestDto() throws Exception {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("test_desc")
                .requestorId(1L)
                .build();

        when(itemRequestServiceDb.addRequest(anyLong(), any(ItemRequestDto.class))).thenReturn(itemRequestDto);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.requestorId", is(itemRequestDto.getRequestorId()), Long.class));

        verify(itemRequestServiceDb, times(1)).addRequest(anyLong(), any(ItemRequestDto.class));
    }
}
