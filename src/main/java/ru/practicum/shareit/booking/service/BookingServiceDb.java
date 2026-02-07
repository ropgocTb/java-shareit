package ru.practicum.shareit.booking.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service("BookingServiceDb")
@Transactional(readOnly = true)
public class BookingServiceDb implements BookingService {
    private final BookingRepository repository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public BookingServiceDb(BookingRepository repository,
                            UserRepository userRepository,
                            ItemRepository itemRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public BookingDto addBooking(Long userId, BookingCreateDto bookingCreateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с таким id не найдена"));
        Item item = itemRepository.findById(bookingCreateDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с таким id не найдена"));

        LocalDateTime start = bookingCreateDto.getStart();
        LocalDateTime end = bookingCreateDto.getEnd();

        if (start == null || end == null || !end.isAfter(start) || !start.isAfter(LocalDateTime.now()))
            throw new ValidationException("Временные промежутки не имеют смысла");

        if (item.getAvailable().equals(false))
            throw new ItemUnavailableException("Вещь недоступна для бронирования");

        BookingDto bookingDto = BookingDto.builder()
                .start(bookingCreateDto.getStart())
                .end(bookingCreateDto.getEnd())
                .item(item)
                .booker(user)
                .status(Status.WAITING)
                .build();

        return BookingMapper.toBookingDto(repository.save(BookingMapper.toBooking(bookingDto)));
    }

    @Transactional(readOnly = false)
    @Override
    public BookingDto editBooking(Long userId, Long bookerId, String approved) {
        Booking booking = repository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("Брони с таким id не найдено"));

        if (!userId.equals(booking.getItem().getOwner().getId()))
            throw new AccessException("Изменять статус брони может только владелец вещи");

        switch (approved) {
            case "true":
                booking.setStatus(Status.APPROVED);
                break;
            case "false":
                booking.setStatus(Status.REJECTED);
                break;
            default:
                throw new InvalidRequestParamException("Параметр запроса при обновлении может быть true/false");
        }

        return BookingMapper.toBookingDto(repository.save(booking));
    }

    @Override
    public BookingDto getBooking(Long id) {
        return BookingMapper.toBookingDto(repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Бронирование с таким id не найдено")));
    }

    @Override
    public List<BookingDto> getUserBookings(Long id, String state) {
        Sort sort = Sort.by(Sort.Direction.DESC, "start");

        List<Booking> bookings = new ArrayList<>();

        bookings = switch (state) {
            case "ALL" -> repository.findAllByBooker_Id(id);
            case "CURRENT" -> repository.findAllByBooker_IdAndStartIsBeforeAndEndIsAfter(id,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    sort);
            case "PAST" -> repository.findAllByBooker_IdAndEndIsBefore(id, LocalDateTime.now(), sort);
            case "FUTURE" -> repository.findAllByBooker_IdAndStartIsAfter(id, LocalDateTime.now(), sort);
            case "WAITING" -> repository.findAllByBooker_IdAndStatusLike(id, Status.WAITING, sort);
            case "REJECTED" -> repository.findAllByBooker_IdAndStatusLike(id, Status.REJECTED, sort);
            case null -> repository.findAllByBooker_Id(id);
            default -> bookings;
        };

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    @Override
    public List<BookingDto> getUserItemsBookings(Long id, String state) {
        Sort sort = Sort.by(Sort.Direction.DESC, "start");

        List<Booking> bookings = new ArrayList<>();

        if (itemRepository.findAllByOwner_Id(id).isEmpty())
            throw new NotFoundException("У пользователя с этим id нет предметов");

        bookings = switch (state) {
            case "ALL" -> repository.findAllByItem_Owner_Id(id);
            case "CURRENT" -> repository.findAllByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(id,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    sort);
            case "PAST" -> repository.findAllByItem_Owner_IdAndEndIsBefore(id, LocalDateTime.now(), sort);
            case "FUTURE" -> repository.findAllByItem_Owner_IdAndStartIsAfter(id, LocalDateTime.now(), sort);
            case "WAITING" -> repository.findAllByItem_Owner_IdAndStatusLike(id, Status.WAITING, sort);
            case "REJECTED" -> repository.findAllByItem_Owner_IdAndStatusLike(id, Status.REJECTED, sort);
            case null -> repository.findAllByItem_Owner_Id(id);
            default -> bookings;
        };

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }
}
