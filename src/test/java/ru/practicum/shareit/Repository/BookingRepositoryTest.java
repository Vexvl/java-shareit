package ru.practicum.shareit.Repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class BookingRepositoryTest {

    @Autowired
    private TestEntityManager em;
    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void searchLastBookingTest() {
        User user = new User(
                null,
                "John",
                "first@user.com"
        );
        em.persist(user);
        em.flush();
        Item item = new Item(
                null,
                "Дрель",
                "Простая дрель",
                true,
                user,
                null
        );
        em.persist(item);
        em.flush();
        Booking booking = new Booking(
                null,
                LocalDateTime.of(2020, 5, 5, 5, 5, 5),
                LocalDateTime.of(2022, 10, 10, 10, 10, 10),
                item,
                user,
                BookingStatus.APPROVED
        );
        em.persist(booking);
        em.flush();
        Iterable<Booking> bookings = bookingRepository.findLastOrderedBookingsByItemId(item.getId());
        assertThat(bookings).hasSize(1).contains(booking);
        em.clear();
    }

    @Test
    void searchNextBookingTest() {
        User user = new User(
                null,
                "John",
                "first@user.com"
        );
        em.persist(user);
        em.flush();
        Item item = new Item(
                null,
                "Дрель",
                "Простая дрель",
                true,
                user,
                null
        );
        em.persist(item);
        em.flush();
        Booking booking = new Booking(
                null,
                LocalDateTime.of(2025, 5, 5, 5, 5, 5),
                LocalDateTime.of(2030, 10, 10, 10, 10, 10),
                item,
                user,
                BookingStatus.APPROVED
        );
        em.persist(booking);
        em.flush();
        Iterable<Booking> bookings = bookingRepository.findNextOrderedBookingsByItemId(item.getId());
        assertThat(bookings).hasSize(1).contains(booking);
        em.clear();
    }
}