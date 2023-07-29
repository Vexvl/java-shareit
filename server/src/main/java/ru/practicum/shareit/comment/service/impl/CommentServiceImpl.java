package src.main.java.ru.practicum.shareit.comment.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import src.main.java.ru.practicum.shareit.booking.exception.NoBookingException;
import src.main.java.ru.practicum.shareit.booking.model.Booking;
import src.main.java.ru.practicum.shareit.booking.model.BookingStatus;
import src.main.java.ru.practicum.shareit.booking.repository.BookingRepository;
import src.main.java.ru.practicum.shareit.comment.dto.CommentDto;
import src.main.java.ru.practicum.shareit.comment.mapper.CommentMapper;
import src.main.java.ru.practicum.shareit.comment.model.Comment;
import src.main.java.ru.practicum.shareit.comment.repository.CommentRepository;
import src.main.java.ru.practicum.shareit.comment.service.CommentService;
import src.main.java.ru.practicum.shareit.item.exception.AbsenceException;
import src.main.java.ru.practicum.shareit.item.model.Item;
import src.main.java.ru.practicum.shareit.item.repository.ItemRepository;
import src.main.java.ru.practicum.shareit.user.model.User;
import src.main.java.ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final BookingRepository bookingRepository;
    private UserRepository userRepository;
    private ItemRepository itemRepository;
    private CommentRepository commentRepository;

    @Override
    public CommentDto addComment(Long ownerId, Long itemId, CommentDto commentDto) {
        User user = userRepository.findById(ownerId).orElseThrow(() -> new AbsenceException("User not exists"));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new AbsenceException("Item not exists"));

        List<Booking> bookings = bookingRepository.findByStatusAndBookerIdAndItemIdAndEndIsBefore(
                BookingStatus.APPROVED, ownerId, itemId, LocalDateTime.now());

        if (bookings.isEmpty()) {
            throw new NoBookingException("No booking before comment");
        }
        Comment comment = commentMapper.toComment(commentDto, item, user);

        return commentMapper.toDto(commentRepository.save(comment));
    }
}