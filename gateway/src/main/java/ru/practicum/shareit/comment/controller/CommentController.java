package ru.practicum.shareit.comment.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.client.CommentClient;
import ru.practicum.shareit.comment.dto.CommentDto;

import javax.validation.Valid;

@Slf4j
@Validated
@AllArgsConstructor
@RestController
@RequestMapping("/items")
public class CommentController {

    private CommentClient commentClient;

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                             @PathVariable("itemId") Long itemId, @RequestBody @Valid CommentDto commentDto) {
        return commentClient.addComment(ownerId, itemId, commentDto);
    }
}