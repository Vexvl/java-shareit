package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
class BookingIntegrationTests {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private BookingRepository bookingRepository;

    private User saveRandomUser() {
        return userRepository.save(User.builder()
                .name("name")
                .email(String.format("%s%s@email.ru", "email", new Random().nextInt(9999)))
                .build());
    }

    private Item saveRandomItem(User owner) {
        return itemRepository.save(Item.builder()
                .name("name")
                .description("desc")
                .available(true)
                .owner(owner)
                .build());
    }

    private Map<String, Long> saveBookingsForSameBookerAndItemOwner() {

        User itemOwner = saveRandomUser();
        User booker = saveRandomUser();

        bookingRepository.save(Booking.builder()
                .item(saveRandomItem(itemOwner))
                .booker(booker)
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusHours(1))
                .status(BookingStatus.APPROVED)
                .build());

        bookingRepository.save(Booking.builder()
                .item(saveRandomItem(itemOwner))
                .booker(booker)
                .start(LocalDateTime.now().minusHours(2))
                .end(LocalDateTime.now().minusHours(1))
                .status(BookingStatus.APPROVED)
                .build());

        bookingRepository.save(Booking.builder()
                .item(saveRandomItem(itemOwner))
                .booker(booker)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .status(BookingStatus.APPROVED)
                .build());

        bookingRepository.save(Booking.builder()
                .item(saveRandomItem(itemOwner))
                .booker(booker)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .status(BookingStatus.WAITING)
                .build());

        bookingRepository.save(Booking.builder()
                .item(saveRandomItem(itemOwner))
                .booker(booker)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .status(BookingStatus.REJECTED)
                .build());

        return Map.of("ItemOwnerId", itemOwner.getId(),
                "BookerId", booker.getId());
    }

    private void assertBookingDtoEquals(BookingDto bookingDto, Booking booking) {
        assertThat(bookingDto.getId(), equalTo(booking.getId()));
        assertThat(bookingDto.getBooker().getId(), equalTo(booking.getBooker().getId()));
        assertThat(bookingDto.getItem().getId(), equalTo(booking.getItem().getId()));
        assertThat(bookingDto.getStart(), equalTo(booking.getStart()));
        assertThat(bookingDto.getEnd(), equalTo(booking.getEnd()));
        assertThat(bookingDto.getStatus(), equalTo(booking.getStatus()));
    }

    @Test
    @Transactional
    void testAddBooking() {
        User booker = saveRandomUser();
        Long bookerId = booker.getId();
        User itemOwner = saveRandomUser();
        Item item = saveRandomItem(itemOwner);
        BookingDto bookingDto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        Long savedBookingId = bookingService.addBooking(bookingDto, bookerId).getId();

        Booking savedBooking = bookingRepository.findById(savedBookingId).get();
        assertThat(savedBooking.getBooker(), equalTo(booker));
        assertThat(savedBooking.getItem(), equalTo(item));
        assertThat(savedBooking.getStart(), equalTo(bookingDto.getStart()));
        assertThat(savedBooking.getEnd(), equalTo(bookingDto.getEnd()));
    }

    @Test
    @Transactional
    void testChangeStatus() {
        User booker = saveRandomUser();
        User itemOwner = saveRandomUser();
        Item item = saveRandomItem(itemOwner);
        boolean approved = true;
        Booking booking = bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build());

        bookingService.editBookingStatus(itemOwner.getId(), booking.getId(), approved);

        Booking updatedBooking = bookingRepository.findById(booking.getId()).get();
        assertThat(updatedBooking.getId(), equalTo(booking.getId()));
        assertThat(updatedBooking.getBooker(), equalTo(booker));
        assertThat(updatedBooking.getItem(), equalTo(item));
        assertThat(updatedBooking.getStart(), equalTo(booking.getStart()));
        assertThat(updatedBooking.getEnd(), equalTo(booking.getEnd()));
        assertThat(updatedBooking.getStatus(), equalTo(BookingStatus.APPROVED));
    }

    @Test
    @Transactional
    void testGetById() {
        User booker = saveRandomUser();
        Long bookerId = booker.getId();
        User itemOwner = saveRandomUser();
        Item item = saveRandomItem(itemOwner);
        Booking booking = bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build());

        BookingDto returnedBooking = bookingService.getById(bookerId, booking.getId());

        assertBookingDtoEquals(returnedBooking, booking);
    }

    private void testGetByStateOwner(String status, BookingStatus expectedState, int expectedSize) {
        bookingRepository.deleteAll();
        saveBookingsForSameBookerAndItemOwner();

        Long ownerId = userRepository.findAll().get(0).getId();

        List<BookingDto> requestedBookings = bookingService.getByStateOwner(ownerId, status, 0, 20);

        assertThat(requestedBookings, hasSize(expectedSize));

        if (expectedState != null) {
            for (BookingDto foundBooking : requestedBookings) {
                assertThat(foundBooking.getStatus(), equalTo(expectedState));
            }
        }
    }

    @Test
    @Transactional
    void testGetByStateOwner_All() {
        testGetByStateOwner("ALL", null, 5);
    }

    @Test
    @Transactional
    void testGetByStateOwner_Current() {
        testGetByStateOwner("CURRENT", BookingStatus.APPROVED, 1);
    }

    @Test
    @Transactional
    void testGetByStateOwner_Past() {
        testGetByStateOwner("PAST", BookingStatus.APPROVED, 1);
    }

    @Test
    @Transactional
    void testGetByStateOwner_Waiting() {
        testGetByStateOwner("WAITING", BookingStatus.WAITING, 1);
    }

    @Test
    @Transactional
    void testGetByStateOwner_Rejected() {
        testGetByStateOwner("REJECTED", BookingStatus.REJECTED, 1);
    }
}