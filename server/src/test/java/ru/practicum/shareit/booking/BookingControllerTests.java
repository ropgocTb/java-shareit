package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingServiceDb;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
//тесты эндпойнтов с моками в слое сервисов
public class BookingControllerTests {
    private MockMvc mvc;
    private ObjectMapper objectMapper;

    @Mock
    private BookingServiceDb bookingServiceDb;

    @InjectMocks
    private BookingController bookingController;

    private User user;
    private Item item;
    private BookingDto bookingDto;
    private BookingCreateDto bookingCreateDto;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(bookingController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        user = User.builder()
                .id(1L)
                .name("test_user")
                .email("test@gmail.com")
                .build();

        item = Item.builder()
                .id(1L)
                .name("test_name")
                .description("test_desc")
                .available(true)
                .owner(user)
                .build();

        bookingDto = BookingDto.builder()
                .item(item)
                .booker(user)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        bookingCreateDto = BookingCreateDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();
    }

    @Test
    void addBookingShouldReturnBookingDto() throws Exception {
        when(bookingServiceDb.addBooking(anyLong(), any(BookingCreateDto.class)))
                .thenReturn(bookingDto);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(user.getId()), Long.class));

        verify(bookingServiceDb, times(1)).addBooking(anyLong(), any(BookingCreateDto.class));
    }

    @Test
    void getBookingShouldReturnBookingDto() throws Exception {
        when(bookingServiceDb.getBooking(anyLong(), anyLong()))
                .thenReturn(bookingDto);

        mvc.perform(get("/bookings/{id}", 1L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(user.getId()), Long.class));

        verify(bookingServiceDb, times(1)).getBooking(anyLong(), anyLong());
    }

    @Test
    void getUserBookingsShouldReturnBookingDtoList() throws Exception {
        when(bookingServiceDb.getUserBookings(anyLong(), anyString()))
                .thenReturn(List.of(bookingDto));

        mvc.perform(get("/bookings?state={state}", "ALL")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.id", is(user.getId()), Long.class));

        verify(bookingServiceDb, times(1)).getUserBookings(anyLong(), anyString());
    }

    @Test
    void getUserItemsBookingsShouldReturnBookingDtoList() throws Exception {
        when(bookingServiceDb.getUserItemsBookings(anyLong(), anyString()))
                .thenReturn(List.of(bookingDto));

        mvc.perform(get("/bookings/owner?state={state}", "ALL")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.id", is(user.getId()), Long.class));

        verify(bookingServiceDb, times(1)).getUserItemsBookings(anyLong(), anyString());
    }

    @Test
    void patchBookingShouldReturnBookingDto() throws Exception {
        when(bookingServiceDb.editBooking(anyLong(), anyLong(), anyString()))
                .thenReturn(bookingDto);

        mvc.perform(patch("/bookings/{id}?approved={approved}", 1L, true)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(user.getId()), Long.class));

        verify(bookingServiceDb, times(1)).editBooking(anyLong(), anyLong(), anyString());
    }
}
