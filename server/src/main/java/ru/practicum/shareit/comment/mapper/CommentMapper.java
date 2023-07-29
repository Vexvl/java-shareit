package src.main.java.ru.practicum.shareit.comment.mapper;

import org.springframework.stereotype.Component;
import src.main.java.ru.practicum.shareit.comment.dto.CommentDto;
import src.main.java.ru.practicum.shareit.comment.model.Comment;
import src.main.java.ru.practicum.shareit.item.model.Item;
import src.main.java.ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Component
public class CommentMapper {

    public Comment toComment(CommentDto commentDto, Item item, User user) {
        return Comment.builder()
                .item(item)
                .author(user)
                .text(commentDto.getText())
                .created(LocalDateTime.now())
                .build();
    }

    public CommentDto toDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }
}