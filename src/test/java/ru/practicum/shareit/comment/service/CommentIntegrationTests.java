package ru.practicum.shareit.comment.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.exception.NoBookingException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CommentIntegrationTests {

    @Autowired
    private CommentService commentService;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    @Test
    @Transactional
    void addComment_NoBookingBeforeComment_ThrowsNoBookingException() {
        User user = saveRandomUser();
        Item item = saveRandomItem(user);
        CommentDto commentDto = CommentDto.builder()
                .text("New Comment")
                .authorName(user.getName())
                .created(LocalDateTime.now())
                .build();

        assertThrows(NoBookingException.class, () -> commentService.addComment(user.getId(), item.getId(), commentDto));
    }

    @Test
    @Transactional
    void addComment_WithApprovedBooking_AddsCommentSuccessfully() {
        User user = saveRandomUser();
        Item item = saveRandomItem(user);
        Booking booking = saveApprovedBooking(user, item);
        CommentDto commentDto = CommentDto.builder()
                .text("New Comment")
                .authorName(user.getName())
                .created(LocalDateTime.now())
                .build();

        CommentDto addedComment = commentService.addComment(user.getId(), item.getId(), commentDto);

        assertNotNull(addedComment.getId());
        assertEquals(commentDto.getText(), addedComment.getText());
        assertEquals(user.getName(), addedComment.getAuthorName());
        assertNotNull(addedComment.getCreated());
    }

    private User saveRandomUser() {
        return userRepository.save(User.builder()
                .name("name")
                .email(String.format("%s%s@email.ru", "email", new Random().nextInt(9999)))
                .build());
    }

    private Item saveRandomItem(User owner) {
        return itemRepository.save(Item.builder()
                .name("name")
                .description("description")
                .available(true)
                .owner(owner)
                .build());
    }

    private Booking saveApprovedBooking(User booker, Item item) {
        LocalDateTime now = LocalDateTime.now();
        return bookingRepository.save(Booking.builder()
                .booker(booker)
                .item(item)
                .status(BookingStatus.APPROVED)
                .start(now.minusDays(2))
                .end(now.minusDays(1))
                .build());
    }
}