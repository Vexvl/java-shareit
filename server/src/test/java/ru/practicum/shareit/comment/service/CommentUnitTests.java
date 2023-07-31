package ru.practicum.shareit.comment.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.exception.NoBookingException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.comment.service.impl.CommentServiceImpl;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentUnitTests {

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

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    void addComment_WithApprovedBooking_AddsCommentSuccessfully() {
        User user = User.builder()
                .id(1L)
                .name("John")
                .email("john@example.com")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("Item 1")
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .status(BookingStatus.APPROVED)
                .booker(user)
                .item(item)
                .end(LocalDateTime.now().minusDays(1))
                .build();

        CommentDto commentDto = CommentDto.builder()
                .text("New Comment")
                .authorName(user.getName())
                .created(LocalDateTime.now())
                .build();

        Comment comment = Comment.builder()
                .id(1L)
                .text(commentDto.getText())
                .author(user)
                .item(item)
                .created(commentDto.getCreated())
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.findByStatusAndBookerIdAndItemIdAndEndIsBefore(
                eq(BookingStatus.APPROVED), eq(user.getId()), eq(item.getId()), Mockito.any(LocalDateTime.class)
        )).thenReturn(Collections.singletonList(booking));
        when(commentMapper.toComment(commentDto, item, user)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentMapper.toDto(comment)).thenReturn(commentDto);

        CommentDto addedComment = commentService.addComment(user.getId(), item.getId(), commentDto);

        assertEquals(commentDto.getText(), addedComment.getText());
        assertEquals(user.getName(), addedComment.getAuthorName());
        assertNotNull(addedComment.getCreated());

        verify(userRepository, times(1)).findById(user.getId());
        verify(itemRepository, times(1)).findById(item.getId());
        verify(bookingRepository, times(1)).findByStatusAndBookerIdAndItemIdAndEndIsBefore(
                eq(BookingStatus.APPROVED), eq(user.getId()), eq(item.getId()), Mockito.any(LocalDateTime.class));
        verify(commentMapper, times(1)).toComment(commentDto, item, user);
        verify(commentRepository, times(1)).save(comment);
        verify(commentMapper, times(1)).toDto(comment);
    }

    @Test
    void addComment_WithNoApprovedBooking_ThrowsNoBookingException() {
        User user = User.builder()
                .id(1L)
                .name("John")
                .email("john@example.com")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("Item 1")
                .build();

        CommentDto commentDto = CommentDto.builder()
                .text("New Comment")
                .authorName(user.getName())
                .created(LocalDateTime.now())
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        when(bookingRepository.findByStatusAndBookerIdAndItemIdAndEndIsBefore(
                eq(BookingStatus.APPROVED), eq(user.getId()), eq(item.getId()), any(LocalDateTime.class)
        )).thenReturn(Collections.emptyList());

        assertThrows(NoBookingException.class,
                () -> commentService.addComment(user.getId(), item.getId(), commentDto));

        verify(userRepository, times(1)).findById(user.getId());
        verify(itemRepository, times(1)).findById(item.getId());
        verify(bookingRepository, times(1)).findByStatusAndBookerIdAndItemIdAndEndIsBefore(
                eq(BookingStatus.APPROVED), eq(user.getId()), eq(item.getId()), any(LocalDateTime.class));
    }
}