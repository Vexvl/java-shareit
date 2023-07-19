package ru.practicum.shareit.comment.service;

import ru.practicum.shareit.comment.dto.CommentDto;

public interface CommentService {
    CommentDto addComment(Long ownerId, Long itemId, CommentDto commentDto);
}