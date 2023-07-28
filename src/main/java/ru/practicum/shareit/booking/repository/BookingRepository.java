package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.item.id = ?1 AND b.start < CURRENT_TIMESTAMP AND b.status = 'APPROVED' " +
            "ORDER BY b.end DESC")
    List<Booking> findLastOrderedBookingsByItemId(Long itemId);

    @Query("SELECT b FROM Booking b WHERE b.item.id = ?1 AND b.start > CURRENT_TIMESTAMP AND b.status = 'APPROVED'" +
            " ORDER BY b.end ASC")
    List<Booking> findNextOrderedBookingsByItemId(Long itemId);

    @Query("SELECT b FROM Booking b WHERE b.item.id IN (:itemIds) AND b.start < CURRENT_TIMESTAMP " +
            "AND b.status = 'APPROVED' " + "ORDER BY b.end DESC")
    List<Booking> findLastOrderedBookingsByItemIds(@Param("itemIds") List<Long> itemIds);

    @Query("SELECT b FROM Booking b WHERE b.item.id IN (:itemIds) AND b.start > CURRENT_TIMESTAMP " +
            "AND b.status = 'APPROVED' " + "ORDER BY b.end ASC")
    List<Booking> findNextOrderedBookingsByItemIds(@Param("itemIds") List<Long> itemIds);

    Page<Booking> findByItemOwnerIdAndStatus(Long ownerId, BookingStatus status, Pageable pageable);

    Page<Booking> findByItemOwnerIdAndStartIsAfter(Long ownerId, LocalDateTime date, Pageable pageable);

    Page<Booking> findByItemOwnerIdAndEndIsBefore(Long ownerId, LocalDateTime date, Pageable pageable);

    Page<Booking> findByItemOwnerIdAndEndIsAfterAndStartIsBefore(Long ownerId, LocalDateTime end,
                                                                 LocalDateTime start, Pageable pageable);

    Page<Booking> findByItemOwnerId(Long ownerId, Pageable pageable);

    Page<Booking> findByBookerId(Long bookerId, Pageable pageable);

    Page<Booking> findByBookerIdAndStartIsAfter(Long bookerId, LocalDateTime date, Pageable pageable);

    Page<Booking> findByBookerIdAndEndIsBefore(Long bookerId, LocalDateTime date, Pageable pageable);

    Page<Booking> findByBookerIdAndEndIsAfterAndStartIsBefore(Long bookerId, LocalDateTime end,
                                                              LocalDateTime start, Pageable pageable);

    Page<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus state, Pageable pageable);

    List<Booking> findByStatusAndBookerIdAndItemIdAndEndIsBefore(BookingStatus bookingStatus, Long ownerId,
                                                                 Long itemId, LocalDateTime now);
}