package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    @Validated
    public ResponseEntity<Object> addItemRequest(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                    @RequestBody @Valid ItemRequestDto itemRequestDto) {
        return itemRequestClient.addItemRequest(itemRequestDto, ownerId);
    }

    @GetMapping
    @Validated
    public ResponseEntity<Object> getAllUserItemRequest(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemRequestClient.getUserResponse(ownerId);
    }

    @GetMapping("/all")
    @Validated
    public ResponseEntity<Object> getAllNotOwner(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                      @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer from,
                                                      @RequestParam(required = false, defaultValue = "10") @Positive Integer size) {
        return itemRequestClient.getAllNotOwner(ownerId, from, size);
    }

    @GetMapping("/{requestId}")
    @Validated
    public ResponseEntity<Object> getItemRequestById(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                     @PathVariable Long requestId) {
        return itemRequestClient.getById(ownerId, requestId);
    }
}