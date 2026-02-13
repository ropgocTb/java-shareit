package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    //findByBookerId
    //state=ALL
    List<Booking> findAllByBooker_Id(Long bookerId, Sort sort);

    //state=CURRENT
    List<Booking> findAllByBooker_IdAndStartIsBeforeAndEndIsAfter(Long bookerId, LocalDateTime start,
                                                                  LocalDateTime end, Sort sort);

    //state=PAST
    List<Booking> findAllByBooker_IdAndEndIsBefore(Long bookerId, LocalDateTime end, Sort sort);

    //state=FUTURE
    List<Booking> findAllByBooker_IdAndStartIsAfter(Long bookerId, LocalDateTime start, Sort sort);

    //state=WAITING, REJECTED
    List<Booking> findAllByBooker_IdAndStatusLike(Long bookerId, Status status, Sort sort);

    //findByItemOwnerId
    //state=ALL
    List<Booking> findAllByItem_Owner_Id(Long ownerId, Sort sort);

    //state=CURRENT
    List<Booking> findAllByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(Long ownerId, LocalDateTime start,
                                                                      LocalDateTime end, Sort sort);

    //state=PAST
    List<Booking> findAllByItem_Owner_IdAndEndIsBefore(Long ownerId, LocalDateTime end, Sort sort);

    //state=FUTURE
    List<Booking> findAllByItem_Owner_IdAndStartIsAfter(Long ownerId, LocalDateTime start, Sort sort);

    //state=WAITING, REJECTED
    List<Booking> findAllByItem_Owner_IdAndStatusLike(Long ownerId, Status status, Sort sort);

    List<Booking> findAllByItem_Id(Long id);

    List<Booking> findAllByItem_IdAndBooker_IdAndEndIsBefore(Long itemId, Long bookerId, LocalDateTime stamp);
}
