package ru.practicum.shareit.Repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void beforeEach() {
        bookingRepository.deleteAll();
    }

    private Item saveRandomItem(User owner) {
        return itemRepository.save(Item.builder()
                .name("name")
                .description("description")
                .available(true)
                .owner(owner)
                .build());
    }

    private Map<String, Long> saveOneBookingForEachBookingStateSearchDtoWithSameBookerAndItemOwner() {
        User itemOwner = saveRandomUser();
        User booker = saveRandomUser();

        bookingRepository.save(Booking.builder().item(saveRandomItem(itemOwner)).booker(booker)
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusHours(1))
                .status(BookingStatus.APPROVED)
                .build());

        bookingRepository.save(Booking.builder().item(saveRandomItem(itemOwner)).booker(booker)
                .start(LocalDateTime.now().minusHours(2))
                .end(LocalDateTime.now().minusHours(1))
                .status(BookingStatus.APPROVED)
                .build());

        bookingRepository.save(Booking.builder().item(saveRandomItem(itemOwner)).booker(booker)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .status(BookingStatus.APPROVED)
                .build());

        bookingRepository.save(Booking.builder().item(saveRandomItem(itemOwner)).booker(booker)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .status(BookingStatus.WAITING)
                .build());

        bookingRepository.save(Booking.builder().item(saveRandomItem(itemOwner)).booker(booker)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .status(BookingStatus.REJECTED)
                .build());

        return Map.of("ItemOwnerId", itemOwner.getId(),
                "BookerId", booker.getId());
    }

    @Test
    void testFindByBookerId_ShouldReturnAllBookingsForBooker() {
        User booker = saveRandomUser();
        Item item = saveRandomItem(saveRandomUser());
        bookingRepository.save(Booking.builder().booker(booker).item(item).status(BookingStatus.WAITING)
                .start(LocalDateTime.now().minusHours(1)).end(LocalDateTime.now().plusHours(1)).build());
        Booking booking2 = bookingRepository.save(Booking.builder().booker(booker).item(item)
                .status(BookingStatus.WAITING).start(LocalDateTime.now().minusHours(1)).end(LocalDateTime.now().plusHours(1))
                .build());
        PageRequest pageRequest = PageRequest.of(1, 1);
        Page<Booking> bookings = bookingRepository.findByBookerId(booker.getId(), pageRequest);
        assertThat(bookings.getTotalPages(), equalTo(2));
        assertThat(bookings.getTotalElements(), equalTo(2L));
        assertEquals(booking2, bookings.getContent().get(0));
    }

    @Test
    void testFindByBookerIdAndStartIsAfter_ShouldReturnFutureBookingsForBooker() {
        PageRequest pageRequest = PageRequest.of(0, 1);
        Long bookerId = saveOneBookingForEachBookingStateSearchDtoWithSameBookerAndItemOwner().get("BookerId");
        LocalDateTime start = LocalDateTime.now();
        Page<Booking> bookings = bookingRepository.findByBookerIdAndStartIsAfter(bookerId, start, pageRequest);
        Booking future = bookings.getContent().get(0);
        assertThat(future.getBooker().getId(), equalTo(bookerId));
        assertTrue(future.getStart().isAfter(LocalDateTime.now()));
    }

    @Test
    void testFindByBookerIdAndEndIsBefore_ShouldReturnPastBookingsForBooker() {
        PageRequest pageRequest = PageRequest.of(0, 1);
        Long bookerId = saveOneBookingForEachBookingStateSearchDtoWithSameBookerAndItemOwner().get("BookerId");
        LocalDateTime end = LocalDateTime.now();
        Page<Booking> bookings = bookingRepository.findByBookerIdAndEndIsBefore(bookerId, end, pageRequest);
        Booking past = bookings.getContent().get(0);
        assertThat(bookings.getTotalPages(), equalTo(1));
        assertThat(bookings.getTotalElements(), equalTo(1L));
        assertThat(past.getBooker().getId(), equalTo(bookerId));
        assertTrue(past.getEnd().isBefore(LocalDateTime.now()));
    }

    @Test
    void testFindByBookerIdAndEndIsAfterAndStartIsBefore_ShouldReturnCurrentBookingsForBooker() {
        PageRequest pageRequest = PageRequest.of(0, 1);
        Long bookerId = saveOneBookingForEachBookingStateSearchDtoWithSameBookerAndItemOwner().get("BookerId");
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = LocalDateTime.now();
        Page<Booking> bookings = bookingRepository.findByBookerIdAndEndIsAfterAndStartIsBefore(bookerId, start, end, pageRequest);
        Booking current = bookings.getContent().get(0);
        assertThat(bookings.getTotalPages(), equalTo(1));
        assertThat(bookings.getTotalElements(), equalTo(1L));
        assertThat(current.getBooker().getId(), equalTo(bookerId));
        assertTrue(current.getStart().isBefore(LocalDateTime.now()));
        assertTrue(current.getEnd().isAfter(LocalDateTime.now()));
    }

    @Test
    void testFindByBookerIdAndState_ShouldReturnBookingsWithGivenStateForBooker() {
        PageRequest pageRequest = PageRequest.of(0, 1);
        Long bookerId = saveOneBookingForEachBookingStateSearchDtoWithSameBookerAndItemOwner().get("BookerId");
        Page<Booking> bookings = bookingRepository.findByBookerIdAndStatus(bookerId, BookingStatus.WAITING, pageRequest);
        Booking booking = bookings.getContent().get(0);
        assertThat(bookings.getTotalPages(), equalTo(1));
        assertThat(bookings.getTotalElements(), equalTo(1L));
        assertThat(booking.getBooker().getId(), equalTo(bookerId));
        assertThat(booking.getStatus(), equalTo(BookingStatus.WAITING));
    }

    @Test
    void testFindByItemOwnerId_ShouldReturnAllBookingsForItemOwner() {
        User itemOwner = saveRandomUser();
        Item item = saveRandomItem(itemOwner);
        PageRequest pageRequest = PageRequest.of(1, 1);
        bookingRepository.save(Booking.builder().booker(saveRandomUser()).item(item).status(BookingStatus.WAITING)
                .start(LocalDateTime.now().minusHours(1)).end(LocalDateTime.now().plusHours(1)).build());
        Booking booking2 = bookingRepository.save(Booking.builder().booker(saveRandomUser()).item(item)
                .status(BookingStatus.WAITING).start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusHours(1)).build());
        Page<Booking> bookings = bookingRepository.findByItemOwnerId(itemOwner.getId(), pageRequest);
        assertThat(bookings.getTotalPages(), equalTo(2));
        assertThat(bookings.getTotalElements(), equalTo(2L));
        assertEquals(booking2, bookings.getContent().get(0));
    }

    @Test
    void testFindByItemOwnerIdAndStartIsAfter_ShouldReturnFutureBookingsForItemOwner() {
        PageRequest pageRequest = PageRequest.of(0, 1);
        Long ownerId = saveOneBookingForEachBookingStateSearchDtoWithSameBookerAndItemOwner().get("ItemOwnerId");
        LocalDateTime start = LocalDateTime.now();
        Page<Booking> bookings = bookingRepository.findByItemOwnerIdAndStartIsAfter(ownerId, start, pageRequest);
        Booking future = bookings.getContent().get(0);
        assertThat(future.getItem().getOwner().getId(), equalTo(ownerId));
        assertTrue(future.getStart().isAfter(LocalDateTime.now()));
    }

    @Test
    void testFindByItemOwnerIdAndEndIsBefore_ShouldReturnPastBookingsForItemOwner() {
        PageRequest pageRequest = PageRequest.of(0, 1);
        Long ownerId = saveOneBookingForEachBookingStateSearchDtoWithSameBookerAndItemOwner().get("ItemOwnerId");
        LocalDateTime end = LocalDateTime.now();
        Page<Booking> bookings = bookingRepository.findByItemOwnerIdAndEndIsBefore(ownerId, end, pageRequest);
        Booking past = bookings.getContent().get(0);
        assertThat(bookings.getTotalPages(), equalTo(1));
        assertThat(bookings.getTotalElements(), equalTo(1L));
        assertThat(past.getItem().getOwner().getId(), equalTo(ownerId));
        assertTrue(past.getEnd().isBefore(LocalDateTime.now()));
    }

    @Test
    void testFindByItemOwnerIdAndEndIsAfterAndStartIsBefore_ShouldReturnCurrentBookingsForItemOwner() {
        PageRequest pageRequest = PageRequest.of(0, 1);
        Long ownerId = saveOneBookingForEachBookingStateSearchDtoWithSameBookerAndItemOwner().get("ItemOwnerId");
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = LocalDateTime.now();
        Page<Booking> bookings = bookingRepository.findByItemOwnerIdAndEndIsAfterAndStartIsBefore(ownerId, start, end, pageRequest);
        Booking current = bookings.getContent().get(0);
        assertThat(bookings.getTotalPages(), equalTo(1));
        assertThat(bookings.getTotalElements(), equalTo(1L));
        assertThat(current.getItem().getOwner().getId(), equalTo(ownerId));
        assertTrue(current.getStart().isBefore(LocalDateTime.now()));
        assertTrue(current.getEnd().isAfter(LocalDateTime.now()));
    }

    @Test
    void testFindByItemOwnerIdAndState_ShouldReturnBookingsWithGivenStateForItemOwner() {
        PageRequest pageRequest = PageRequest.of(0, 1);
        Long ownerId = saveOneBookingForEachBookingStateSearchDtoWithSameBookerAndItemOwner().get("ItemOwnerId");
        Page<Booking> bookings = bookingRepository.findByItemOwnerIdAndStatus(ownerId, BookingStatus.WAITING, pageRequest);
        Booking booking = bookings.getContent().get(0);
        assertThat(bookings.getTotalPages(), equalTo(1));
        assertThat(bookings.getTotalElements(), equalTo(1L));
        assertThat(booking.getItem().getOwner().getId(), equalTo(ownerId));
        assertThat(booking.getStatus(), equalTo(BookingStatus.WAITING));
    }

    private User saveRandomUser() {
        return userRepository.save(User.builder()
                .name("name")
                .email(String.format("%s%s@email.ru", "email", new Random().nextInt(9999)))
                .build());
    }
}