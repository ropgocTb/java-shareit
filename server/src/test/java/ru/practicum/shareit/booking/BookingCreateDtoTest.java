package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.booking.dto.BookingCreateDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingCreateDtoTest {

    @Autowired
    private ObjectMapper objectMapper;

    private BookingCreateDto bookingCreateDto;

    @BeforeEach
    void setUp() {
        bookingCreateDto = BookingCreateDto.builder()
                .itemId(1L)
                .start(LocalDateTime.of(2026, 2, 17, 13, 14, 0))
                .end(LocalDateTime.of(2026, 2, 17, 14, 0, 0))
                .build();
    }

    @Test
    void testSerialize() throws Exception {
        String json = objectMapper.writeValueAsString(bookingCreateDto);
        String expectedJson = "{\"itemId\":1,\"start\":\"2026-02-17T13:14:00\",\"end\":\"2026-02-17T14:00:00\"}";

        assertThat(json).isEqualTo(expectedJson);
    }

    @Test
    void testDeserialize() throws Exception {
        String json = "{\"itemId\":1,\"start\":\"2026-02-17T13:14:00\",\"end\":\"2026-02-17T14:00:00\"}";

        BookingCreateDto deserializedDto = objectMapper.readValue(json, BookingCreateDto.class);

        assertThat(deserializedDto.getItemId()).isEqualTo(bookingCreateDto.getItemId());
        assertThat(deserializedDto.getStart()).isEqualTo(bookingCreateDto.getStart());
        assertThat(deserializedDto.getEnd()).isEqualTo(bookingCreateDto.getEnd());
    }
}
