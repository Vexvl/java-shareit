package ru.practicum.shareit.Integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.exception.UnsupportedStatusException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BookingTest {

    private final EntityManager em;
    private final BookingService bookingService;

    @Test
    void createBookingTest() {
        User testUser = new User(
                null,
                "John",
                "first@user.com"
        );
        em.persist(testUser);
        em.flush();
        Item item = new Item(
                null,
                "Дрель",
                "Простая дрель",
                true,
                testUser,
                null
        );
        em.persist(item);
        em.flush();
        User anotherTestUser = new User(
                null,
                "NotJohn",
                "second@user.com"
        );
        em.persist(anotherTestUser);
        em.flush();
        BookingDto testBookingDto = new BookingDto(
                null,
                LocalDateTime.of(2023, 10, 10, 10, 10, 10),
                LocalDateTime.of(2030, 10, 10, 10, 10, 10),
                null,
                item.getId(),
                null,
                null,
                null
        );
        bookingService.addBooking(testBookingDto, anotherTestUser.getId());
        TypedQuery<Booking> query = em.createQuery("Select b from Booking b", Booking.class);
        List<Booking> getBookings = query.getResultList();
        assertEquals(1, getBookings.size());
        assertEquals(anotherTestUser.getId(), getBookings.get(0).getBooker().getId());
        assertEquals(item.getId(), getBookings.get(0).getItem().getId());
        assertEquals(BookingStatus.WAITING, getBookings.get(0).getStatus());
        assertEquals(LocalDateTime.of(2023, 10, 10, 10, 10, 10), getBookings.get(0).getStart());
        assertEquals(LocalDateTime.of(2030, 10, 10, 10, 10, 10), getBookings.get(0).getEnd());
        em.clear();
    }

    @Test
    void changeBookingStatusTest() {
        User testUser = new User(
                null,
                "John",
                "first@user.com"
        );
        em.persist(testUser);
        em.flush();
        Item item = new Item(
                null,
                "Дрель",
                "Простая дрель",
                true,
                testUser,
                null
        );
        em.persist(item);
        em.flush();
        User anotherTestUser = new User(
                null,
                "NotJohn",
                "second@user.com"
        );
        em.persist(anotherTestUser);
        em.flush();
        Booking booking = new Booking(
                null,
                LocalDateTime.of(2023, 10, 10, 10, 10, 10),
                LocalDateTime.of(2030, 10, 10, 10, 10, 10),
                item,
                anotherTestUser,
                BookingStatus.WAITING
        );
        em.persist(booking);
        em.flush();
        BookingDto getBookingDto = bookingService.editBookingStatus(testUser.getId(), booking.getId(), Boolean.FALSE);
        assertEquals(anotherTestUser.getId(), getBookingDto.getBooker().getId());
        assertEquals(item.getId(), getBookingDto.getItem().getId());
        assertEquals(BookingStatus.REJECTED, getBookingDto.getStatus());
        assertEquals(LocalDateTime.of(2023, 10, 10, 10, 10, 10), getBookingDto.getStart());
        assertEquals(LocalDateTime.of(2030, 10, 10, 10, 10, 10), getBookingDto.getEnd());
    }

    @Test
    void getUserItemsBookingTest() {
        User testUser = new User(
                null,
                "John",
                "first@user.com"
        );
        em.persist(testUser);
        em.flush();
        Item item = new Item(
                null,
                "Дрель",
                "Простая дрель",
                true,
                testUser,
                null
        );
        em.persist(item);
        em.flush();
        User anotherTestUser = new User(
                null,
                "NotJohn",
                "second@user.com"
        );
        em.persist(anotherTestUser);
        em.flush();
        Booking booking = new Booking(
                null,
                LocalDateTime.of(2013, 10, 10, 10, 10, 10),
                LocalDateTime.of(2020, 10, 10, 10, 10, 10),
                item,
                anotherTestUser,
                BookingStatus.APPROVED
        );
        em.persist(booking);
        em.flush();
        List<BookingDto> getBookings = bookingService.getByStateOwner(testUser.getId(), "PAST", 0, 5);
        assertEquals(1, getBookings.size());
        assertEquals(anotherTestUser.getId(), getBookings.get(0).getBooker().getId());
        assertEquals(item.getId(), getBookings.get(0).getItem().getId());
        assertEquals(BookingStatus.APPROVED, getBookings.get(0).getStatus());
        assertEquals(LocalDateTime.of(2013, 10, 10, 10, 10, 10), getBookings.get(0).getStart());
        assertEquals(LocalDateTime.of(2020, 10, 10, 10, 10, 10), getBookings.get(0).getEnd());
        assertTrue(bookingService.getByStateOwner(testUser.getId(), "CURRENT", 0, 5).isEmpty());
        assertTrue(bookingService.getByStateOwner(testUser.getId(), "FUTURE", 0, 5).isEmpty());
        assertTrue(bookingService.getByStateOwner(testUser.getId(), "WAITING", 0, 5).isEmpty());
        assertTrue(bookingService.getByStateOwner(testUser.getId(), "REJECTED", 0, 5).isEmpty());
        assertEquals(1, bookingService.getByStateOwner(testUser.getId(), "AlL", 0, 5).size());
        UnsupportedStatusException ex = Assertions.assertThrows(
                UnsupportedStatusException.class, () -> bookingService.getByStateOwner(
                        testUser.getId(), "UnsupState", 0, 5));
        assertEquals("Unknown state: UNSUPPORTED_STATUS", ex.getMessage());
        em.clear();
    }
}