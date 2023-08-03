package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/items")
@Slf4j
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> addItem(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                             @Valid @RequestBody ItemDto itemDto) {
        return itemClient.addItem(itemDto, ownerId);
    }

    @GetMapping()
    public ResponseEntity<Object> getAllUserItem(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                 @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer from,
                                                 @RequestParam(required = false, defaultValue = "10") @Positive Integer size) {
        return itemClient.getAllUserItem(ownerId, from, size);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                              @PathVariable Long itemId) {
        return itemClient.getById(itemId, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> editItem(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                             @PathVariable Long itemId,
                                             @RequestBody ItemDto itemDto) {
        return itemClient.editItem(ownerId, itemId, itemDto);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItem(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                             @RequestParam String text,
                                             @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer from,
                                             @RequestParam(required = false, defaultValue = "10") @Positive Integer size) {
        return itemClient.searchItems(ownerId, text, from, size);
    }
}