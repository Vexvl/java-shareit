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
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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

    @Test
    @Transactional
    void addBooking_SaveToDB() {
        User booker = saveRandomUser();
        Long bookerId = booker.getId();
        User itemOwner = saveRandomUser();
        Item item = saveRandomItem(itemOwner);
        BookingDto bookingCreationDto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        Long savedBookingId = bookingService.addBooking(bookingCreationDto, bookerId).getId();

        Booking savedBooking = bookingRepository.findById(savedBookingId).get();
        assertThat(savedBooking.getBooker(), equalTo(booker));
        assertThat(savedBooking.getItem(), equalTo(item));
        assertThat(savedBooking.getStart(), equalTo(bookingCreationDto.getStart()));
        assertThat(savedBooking.getEnd(), equalTo(bookingCreationDto.getEnd()));
    }

    @Test
    void addBooking_ReturnDto() {
        User booker = saveRandomUser();
        Long bookerId = booker.getId();
        User itemOwner = saveRandomUser();
        Item item = saveRandomItem(itemOwner);
        BookingDto bookingCreationDto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        BookingDto returnedBooking = bookingService.addBooking(bookingCreationDto, bookerId);

        assertThat(returnedBooking.getBooker().getId(), equalTo(bookerId));
        assertThat(returnedBooking.getItem().getId(), equalTo(item.getId()));
        assertThat(returnedBooking.getStart(), equalTo(bookingCreationDto.getStart()));
        assertThat(returnedBooking.getEnd(), equalTo(bookingCreationDto.getEnd()));
    }

    @Test
    @Transactional
    void changeStatus_SaveToDB() {
        User booker = saveRandomUser();
        Long bookerId = booker.getId();
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
    void changeStatus_ReturnDto() {
        User booker = saveRandomUser();
        Long bookerId = booker.getId();
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

        BookingDto returnedBooking = bookingService.editBookingStatus(itemOwner.getId(), booking.getId(), approved);

        assertThat(returnedBooking.getId(), equalTo(booking.getId()));
        assertThat(returnedBooking.getBooker().getId(), equalTo(bookerId));
        assertThat(returnedBooking.getItem().getId(), equalTo(item.getId()));
        assertThat(returnedBooking.getStatus(), equalTo(BookingStatus.APPROVED));
    }

    @Test
    @Transactional
    void getById_ReturnDto() {
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

        assertThat(returnedBooking.getId(), equalTo(booking.getId()));
        assertThat(returnedBooking.getBooker().getId(), equalTo(bookerId));
        assertThat(returnedBooking.getItem().getId(), equalTo(item.getId()));
        assertThat(returnedBooking.getStart(), equalTo(booking.getStart()));
        assertThat(returnedBooking.getEnd(), equalTo(booking.getEnd()));
    }

    private User saveRandomUser() {
        return userRepository.save(User.builder()
                .name("name")
                .email(String.format("email%s@example.com", new Random().nextInt(9999)))
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
}