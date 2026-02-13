package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService service;

    public BookingController(@Qualifier("BookingServiceDb") BookingService service) {
        this.service = service;
    }

    @GetMapping
    public List<BookingDto> getBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                        @RequestParam(name = "state", required = false) String state) {
        return service.getUserBookings(userId, state);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @PathVariable(name = "bookingId") Long id) {
        return service.getBooking(userId, id);
    }

    @GetMapping("/owner")
    public List<BookingDto> getUserItemsBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @RequestParam(name = "state", required = false) String state) {
        return service.getUserItemsBookings(userId, state);
    }

    @PostMapping
    public BookingDto postBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                  @Valid @RequestBody BookingCreateDto bookingCreateDto) {
        return service.addBooking(userId, bookingCreateDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto patchBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                   @PathVariable(name = "bookingId") Long bookingId,
                                   @RequestParam(name = "approved") String approved) {
        return service.editBooking(userId, bookingId, approved);
    }
}
