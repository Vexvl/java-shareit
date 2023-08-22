package ru.practicum.shareit.comment.dto;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class CommentMapperTests {

    @Mock
    private Item item;

    @Mock
    private User user;

    private CommentMapper commentMapper;

    @Before
    public void setUp() {
        commentMapper = new CommentMapper();
    }

    @Test
    public void testToComment() {
        CommentDto commentDto = CommentDto.builder()
                .text("This is a comment.")
                .build();

        Comment comment = commentMapper.toComment(commentDto, item, user);

        assertEquals(commentDto.getText(), comment.getText());
        assertEquals(item, comment.getItem());
        assertEquals(user, comment.getAuthor());
    }

    @Test
    public void testToDto() {
        Comment comment = Comment.builder()
                .id(1L)
                .text("This is a comment.")
                .author(user)
                .created(LocalDateTime.now())
                .build();

        CommentDto commentDto = commentMapper.toDto(comment);

        assertEquals(comment.getId(), commentDto.getId());
        assertEquals(comment.getText(), commentDto.getText());
        assertEquals(comment.getAuthor().getName(), commentDto.getAuthorName());
        assertEquals(comment.getCreated(), commentDto.getCreated());
    }
}