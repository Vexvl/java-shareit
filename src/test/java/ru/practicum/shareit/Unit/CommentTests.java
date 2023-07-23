package ru.practicum.shareit.Unit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CommentTests {

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CommentRepository commentRepository;

    @Test
    public void testAddCommentWithValidData() {
        Long ownerId = 1L;
        Long itemId = 2L;
        String commentText = "This is a comment.";

        User user = User.builder()
                .id(ownerId)
                .name("John")
                .email("john@example.com")
                .build();

        Item item = Item.builder()
                .id(itemId)
                .name("Item 1")
                .description("Item description")
                .build();

        CommentDto commentDto = CommentDto.builder()
                .text(commentText)
                .build();

        List<Booking> bookings = new ArrayList<>();
        bookings.add(Booking.builder()
                .id(1L)
                .status(BookingStatus.APPROVED)
                .booker(user)
                .item(item)
                .start(LocalDateTime.now().minusHours(2))
                .end(LocalDateTime.now().minusHours(1))
                .build());

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findByStatusAndBookerIdAndItemIdAndEndIsBefore(any(), any(), any(), any()))
                .thenReturn(bookings);

        Comment comment = Comment.builder()
                .text(commentText)
                .author(user)
                .item(item)
                .created(LocalDateTime.now())
                .build();

        when(commentMapper.toComment(commentDto, item, user)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(comment);

    }
}