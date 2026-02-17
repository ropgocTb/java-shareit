package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceDb;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
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
public class BookingIntegrationTests {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingServiceDb bookingService;

    @Autowired
    private MockMvc mvc;

    private User userWithItem;
    private User userMakesBooking;
    private Item item;
    private Booking booking;
    private BookingCreateDto bookingCreateDto;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        userWithItem = User.builder()
                .name("test_user")
                .email("test@test.test")
                .build();

        userRepository.save(userWithItem);

        userMakesBooking = User.builder()
                .name("test_test_user")
                .email("test123@gmail.com")
                .build();

        userRepository.save(userMakesBooking);

        item = Item.builder()
                .name("test_item")
                .description("item_desc")
                .available(true)
                .owner(userWithItem)
                .build();

        itemRepository.save(item);

        BookingDto bookingDto = BookingDto.builder().build();

        bookingCreateDto = BookingCreateDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusSeconds(3).truncatedTo(ChronoUnit.SECONDS))
                .end(LocalDateTime.now().plusDays(3).plusSeconds(3).truncatedTo(ChronoUnit.SECONDS))
                .build();

        booking = Booking.builder()
                .start(bookingCreateDto.getStart())
                .end(bookingCreateDto.getEnd())
                .item(item)
                .booker(userMakesBooking)
                .status(Status.WAITING)
                .build();
    }

    @Test
    public void addBookingTest() throws Exception {
        mvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(bookingCreateDto))
                        .header("X-Sharer-User-Id", userMakesBooking.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.start", is(bookingCreateDto.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingCreateDto.getEnd().toString())))
                .andExpect(jsonPath("$.item").exists())
                .andExpect(jsonPath("$.booker").exists())
                .andExpect(jsonPath("$.status", is(Status.WAITING.name())));
    }

    @Test
    public void addBookingUnavailableItemTestShouldThrow400() throws Exception {
        item = Item.builder()
                .name("test_item")
                .description("item_desc")
                .available(false)
                .owner(userWithItem)
                .build();
        Item savedUnavailableItem = itemRepository.save(item);

        bookingCreateDto.setItemId(savedUnavailableItem.getId());

        mvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(bookingCreateDto))
                        .header("X-Sharer-User-Id", userMakesBooking.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    //получение информации о бронировании инициатором
    @Test
    public void getBookingByBookerTest() throws Exception {
        Booking savedBooking = bookingRepository.save(booking);

        mvc.perform(get("/bookings/{id}", savedBooking.getId())
                        .header("X-Sharer-User-Id", userMakesBooking.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedBooking.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(savedBooking.getStart().toString())))
                .andExpect(jsonPath("$.end", is(savedBooking.getEnd().toString())))
                .andExpect(jsonPath("$.item").exists())
                .andExpect(jsonPath("$.booker").exists())
                .andExpect(jsonPath("$.status", is(savedBooking.getStatus().name())));
    }

    //получение информации о бронировании владельцем
    @Test
    public void getBookingByOwnerTest() throws Exception {
        Booking savedBooking = bookingRepository.save(booking);

        mvc.perform(get("/bookings/{id}", savedBooking.getId())
                        .header("X-Sharer-User-Id", userWithItem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedBooking.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(savedBooking.getStart().toString())))
                .andExpect(jsonPath("$.end", is(savedBooking.getEnd().toString())))
                .andExpect(jsonPath("$.item").exists())
                .andExpect(jsonPath("$.booker").exists())
                .andExpect(jsonPath("$.status", is(savedBooking.getStatus().name())));
    }

    //получение информации о бронировании другим пользователем
    @Test
    public void getBookingByOtherTest() throws Exception {
        Booking savedBooking = bookingRepository.save(booking);

        mvc.perform(get("/bookings/{id}", savedBooking.getId())
                        .header("X-Sharer-User-Id", userWithItem.getId() + 29))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getBookingsTest() throws Exception {
        Booking savedBooking = bookingRepository.save(booking);

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userMakesBooking.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(savedBooking.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(savedBooking.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(savedBooking.getEnd().toString())))
                .andExpect(jsonPath("$[0].item").exists())
                .andExpect(jsonPath("$[0].booker").exists())
                .andExpect(jsonPath("$[0].status", is(savedBooking.getStatus().name())));

        mvc.perform(get("/bookings?state={state}", "ALL")
                        .header("X-Sharer-User-Id", userMakesBooking.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(savedBooking.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(savedBooking.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(savedBooking.getEnd().toString())))
                .andExpect(jsonPath("$[0].item").exists())
                .andExpect(jsonPath("$[0].booker").exists())
                .andExpect(jsonPath("$[0].status", is(savedBooking.getStatus().name())));

        mvc.perform(get("/bookings?state={state}", "WAITING")
                        .header("X-Sharer-User-Id", userMakesBooking.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(savedBooking.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(savedBooking.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(savedBooking.getEnd().toString())))
                .andExpect(jsonPath("$[0].item").exists())
                .andExpect(jsonPath("$[0].booker").exists())
                .andExpect(jsonPath("$[0].status", is(savedBooking.getStatus().name())));

        mvc.perform(get("/bookings?state={state}", "FUTURE")
                        .header("X-Sharer-User-Id", userMakesBooking.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(savedBooking.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(savedBooking.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(savedBooking.getEnd().toString())))
                .andExpect(jsonPath("$[0].item").exists())
                .andExpect(jsonPath("$[0].booker").exists())
                .andExpect(jsonPath("$[0].status", is(savedBooking.getStatus().name())));
    }

    @Test
    public void getUserItemsBookings() throws Exception {
        Booking savedBooking = bookingRepository.save(booking);

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userWithItem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(savedBooking.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(savedBooking.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(savedBooking.getEnd().toString())))
                .andExpect(jsonPath("$[0].item").exists())
                .andExpect(jsonPath("$[0].booker").exists())
                .andExpect(jsonPath("$[0].status", is(savedBooking.getStatus().name())));

        mvc.perform(get("/bookings/owner?state={state}", "ALL")
                        .header("X-Sharer-User-Id", userWithItem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(savedBooking.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(savedBooking.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(savedBooking.getEnd().toString())))
                .andExpect(jsonPath("$[0].item").exists())
                .andExpect(jsonPath("$[0].booker").exists())
                .andExpect(jsonPath("$[0].status", is(savedBooking.getStatus().name())));

        mvc.perform(get("/bookings/owner?state={state}", "WAITING")
                        .header("X-Sharer-User-Id", userWithItem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(savedBooking.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(savedBooking.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(savedBooking.getEnd().toString())))
                .andExpect(jsonPath("$[0].item").exists())
                .andExpect(jsonPath("$[0].booker").exists())
                .andExpect(jsonPath("$[0].status", is(savedBooking.getStatus().name())));

        mvc.perform(get("/bookings/owner?state={state}", "FUTURE")
                        .header("X-Sharer-User-Id", userWithItem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(savedBooking.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(savedBooking.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(savedBooking.getEnd().toString())))
                .andExpect(jsonPath("$[0].item").exists())
                .andExpect(jsonPath("$[0].booker").exists())
                .andExpect(jsonPath("$[0].status", is(savedBooking.getStatus().name())));
    }

    @Test
    public void editBookingTestShouldReturnApprovedBooking() throws Exception {
        Booking savedBooking = bookingRepository.save(booking);

        mvc.perform(patch("/bookings/{id}?approved={approved}", savedBooking.getId(), true)
                        .header("X-Sharer-User-Id", userWithItem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedBooking.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(savedBooking.getStart().toString())))
                .andExpect(jsonPath("$.end", is(savedBooking.getEnd().toString())))
                .andExpect(jsonPath("$.item").exists())
                .andExpect(jsonPath("$.booker").exists())
                .andExpect(jsonPath("$.status", is(Status.APPROVED.name())));

        Thread.sleep(5000);
        //получение пользователем
        mvc.perform(get("/bookings?state={state}", "CURRENT")
                        .header("X-Sharer-User-Id", userMakesBooking.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(savedBooking.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(savedBooking.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(savedBooking.getEnd().toString())))
                .andExpect(jsonPath("$[0].item").exists())
                .andExpect(jsonPath("$[0].booker").exists())
                .andExpect(jsonPath("$[0].status", is(savedBooking.getStatus().name())));

        //получение владельцем вещи
        mvc.perform(get("/bookings/owner?state={state}", "CURRENT")
                        .header("X-Sharer-User-Id", userWithItem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(savedBooking.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(savedBooking.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(savedBooking.getEnd().toString())))
                .andExpect(jsonPath("$[0].item").exists())
                .andExpect(jsonPath("$[0].booker").exists())
                .andExpect(jsonPath("$[0].status", is(savedBooking.getStatus().name())));
    }

    @Test
    public void editBookingByWrongUserTestShouldThrowInvalidParamException() throws Exception {
        Booking savedBooking = bookingRepository.save(booking);

        mvc.perform(patch("/bookings/{id}?approved={approved}", savedBooking.getId(), true)
                        .header("X-Sharer-User-Id", userMakesBooking.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void editBookingWithWrongParamShouldThrowValidationException() throws Exception {
        Booking savedBooking = bookingRepository.save(booking);

        mvc.perform(patch("/bookings/{id}?approved={approved}", savedBooking.getId(), "asdfasdf")
                        .header("X-Sharer-User-Id", userWithItem.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void editBookingTestShouldReturnRejectedBooking() throws Exception {
        Booking savedBooking = bookingRepository.save(booking);

        mvc.perform(patch("/bookings/{id}?approved={approved}", savedBooking.getId(), false)
                        .header("X-Sharer-User-Id", userWithItem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedBooking.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(savedBooking.getStart().toString())))
                .andExpect(jsonPath("$.end", is(savedBooking.getEnd().toString())))
                .andExpect(jsonPath("$.item").exists())
                .andExpect(jsonPath("$.booker").exists())
                .andExpect(jsonPath("$.status", is(Status.REJECTED.name())));

        //получение отказанных броней пользователем
        mvc.perform(get("/bookings?state={state}", "REJECTED")
                        .header("X-Sharer-User-Id", userMakesBooking.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(savedBooking.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(savedBooking.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(savedBooking.getEnd().toString())))
                .andExpect(jsonPath("$[0].item").exists())
                .andExpect(jsonPath("$[0].booker").exists())
                .andExpect(jsonPath("$[0].status", is(savedBooking.getStatus().name())));

        //получение отказанных броней владельцем
        mvc.perform(get("/bookings/owner?state={state}", "REJECTED")
                        .header("X-Sharer-User-Id", userWithItem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(savedBooking.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(savedBooking.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(savedBooking.getEnd().toString())))
                .andExpect(jsonPath("$[0].item").exists())
                .andExpect(jsonPath("$[0].booker").exists())
                .andExpect(jsonPath("$[0].status", is(savedBooking.getStatus().name())));
    }

    @Test
    public void gettingPastBookingsByOwnerAndByUserShouldReturnListOfBookingDto() throws Exception {
        Item newItem = Item.builder().build();

        booking.setStart(LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.SECONDS));
        booking.setEnd(LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.SECONDS));

        Booking savedBooking = bookingRepository.save(booking);

        mvc.perform(get("/bookings?state={state}", "PAST")
                        .header("X-Sharer-User-Id", userMakesBooking.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(savedBooking.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(savedBooking.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(savedBooking.getEnd().toString())))
                .andExpect(jsonPath("$[0].item").exists())
                .andExpect(jsonPath("$[0].booker").exists())
                .andExpect(jsonPath("$[0].status", is(savedBooking.getStatus().name())));

        mvc.perform(get("/bookings/owner?state={state}", "PAST")
                        .header("X-Sharer-User-Id", userWithItem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(savedBooking.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(savedBooking.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(savedBooking.getEnd().toString())))
                .andExpect(jsonPath("$[0].item").exists())
                .andExpect(jsonPath("$[0].booker").exists())
                .andExpect(jsonPath("$[0].status", is(savedBooking.getStatus().name())));
    }
}
