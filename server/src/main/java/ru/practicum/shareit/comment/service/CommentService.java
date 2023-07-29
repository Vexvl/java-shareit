package src.main.java.ru.practicum.shareit.comment.service;

import src.main.java.ru.practicum.shareit.comment.dto.CommentDto;

public interface CommentService {
    CommentDto addComment(Long ownerId, Long itemId, CommentDto commentDto);
}