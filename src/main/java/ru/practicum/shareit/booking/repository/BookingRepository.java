package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b WHERE b.item.id = ?1 AND b.start < CURRENT_TIMESTAMP AND b.status = 'APPROVED'")
    List<Booking> findLastOrderedBookingsByItemId(Long itemId);

    @Query("SELECT b FROM Booking b WHERE b.item.id = ?1 AND b.start > CURRENT_TIMESTAMP AND b.status = 'APPROVED'")
    List<Booking> findNextOrderedBookingsByItemId(Long itemId);

    Optional<Booking> findTopByStatusAndItemIdAndStartIsBefore(BookingStatus status,
                                                               Long itemId,
                                                               LocalDateTime date,
                                                               Sort sort);

    Optional<Booking> findTopByStatusAndItemIdAndStartAfter(BookingStatus status,
                                                            Long itemId,
                                                            LocalDateTime date,
                                                            Sort sort);

    List<Booking> findByItemOwnerIdAndStatus(Long ownerId, BookingStatus status);

    List<Booking> findByItemOwnerIdAndStartIsAfter(Long ownerId, LocalDateTime date, Sort sort);

    List<Booking> findByItemOwnerIdAndEndIsBefore(Long ownerId, LocalDateTime date, Sort sort);

    List<Booking> findByItemOwnerIdAndEndIsAfterAndStartIsBefore(Long bookerId, LocalDateTime end, LocalDateTime start, Sort sort);

    List<Booking> findByItemOwnerId(Long ownerId, Sort sort);

    List<Booking> findByBookerId(Long bookerId, Sort sort);

    List<Booking> findByBookerIdAndStartIsAfter(Long bookerId, LocalDateTime date, Sort sort);

    List<Booking> findByBookerIdAndEndIsBefore(Long bookerId, LocalDateTime date, Sort sort);

    List<Booking> findByBookerIdAndEndIsAfterAndStartIsBefore(Long bookerId, LocalDateTime end, LocalDateTime start, Sort sort);

    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus state);

    List<Booking> findByStatusAndBookerIdAndItemIdAndEndIsBefore(BookingStatus bookingStatus, Long ownerId, Long itemId, LocalDateTime now);
}