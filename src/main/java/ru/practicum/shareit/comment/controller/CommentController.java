package ru.practicum.shareit.comment.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.service.CommentService;

import javax.validation.Valid;

@Slf4j
@Validated
@AllArgsConstructor
@RestController
@RequestMapping("/items")
public class CommentController {

    private CommentService commentService;

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                 @PathVariable("itemId") Long itemId, @RequestBody @Valid CommentDto commentDto) {
        return commentService.addComment(ownerId, itemId, commentDto);
    }
}