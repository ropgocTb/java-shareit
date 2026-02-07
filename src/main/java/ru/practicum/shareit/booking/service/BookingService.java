package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto addBooking(Long userId, BookingCreateDto bookingCreateDto);

    BookingDto editBooking(Long userId, Long bookerId, String approved);

    BookingDto getBooking(Long id);

    List<BookingDto> getUserBookings(Long id, String state);

    List<BookingDto> getUserItemsBookings(Long id, String state);
}
