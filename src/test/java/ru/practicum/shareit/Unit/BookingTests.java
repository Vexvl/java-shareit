package ru.practicum.shareit.Unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.impl.BookingServiceImpl;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.AbsenceException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingTests {

    @Mock
    private BookingRepository bookingRepository;
    @InjectMocks
    private BookingServiceImpl bookingService;

    private final User user = new User(1L, "user1", "user@user.com");
    private final Item item = new Item(2L, "Дрель", "Простая дрель", true, user, null);
    private final BookingDto testBookingDto = new BookingDto(
            1L,
            LocalDateTime.of(2020, 10, 10, 10, 10, 10),
            LocalDateTime.of(2030, 10, 10, 10, 10, 10),
            BookingStatus.APPROVED,
            2L,
            1L,
            new UserDto(1L, "user1", "user@user.com"),
            new ItemDto(2L, "Дрель", "Простая дрель", true, null)
    );

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addBooking_InvalidDate_ThrowsAbsenceException() {
        testBookingDto.setStart(LocalDateTime.of(2030, 10, 10, 10, 10, 10));
        testBookingDto.setEnd(LocalDateTime.of(2020, 10, 10, 10, 10, 10));
        assertThrows(AbsenceException.class, () -> bookingService.addBooking(testBookingDto, 1L));
    }

    @Test
    void addBooking_ItemNotAvailable_ThrowsAbsenceException() {
        item.setAvailable(false);
        assertThrows(AbsenceException.class, () -> bookingService.addBooking(testBookingDto, 1L));
    }

    @Test
    void addBooking_OwnerBooking_ThrowsNoBookingException() {
        testBookingDto.setBookerId(item.getOwner().getId());
        assertThrows(AbsenceException.class, () -> bookingService.addBooking(testBookingDto, 1L));
    }


    @Test
    void editBookingStatus_InvalidStatus_ThrowsInvalidStatusException() {
        Booking booking = new Booking();
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(NullPointerException.class, () -> bookingService.editBookingStatus(item.getOwner().getId(), 1L, true));
    }
}