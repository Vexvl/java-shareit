package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto addItemRequest(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                         @RequestBody @Valid ItemRequestDto itemRequestDto) {
        return itemRequestService.addItemRequest(itemRequestDto, ownerId);
    }

    @GetMapping
    public List<ItemRequestDto> getAllUserItem(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemRequestService.getOwnerResponse(ownerId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getOtherItemRequests(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                     @RequestParam(required = false, defaultValue = "0")
                                                     @Positive Integer from,
                                                     @RequestParam(required = false, defaultValue = "10")
                                                     @Positive Integer size) {
        return itemRequestService.getAllNotOwner(ownerId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getItemRequest(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                         @PathVariable Long requestId) {
        return itemRequestService.getItemRequestById(ownerId, requestId);
    }
}