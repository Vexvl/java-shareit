package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBookingComments;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemDto addItem(@RequestHeader("X-Sharer-User-Id") Long ownerId, @Valid @RequestBody ItemDto itemDto) {
        return itemService.addItem(ownerId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto editItem(@RequestHeader("X-Sharer-User-Id") Long ownerId, @PathVariable("itemId") Long itemId,
                            @RequestBody ItemDto itemDto) {
        return itemService.editItem(ownerId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDtoBookingComments getItem(@RequestHeader("X-Sharer-User-Id") Long ownerId, @PathVariable Long itemId) {
        return itemService.getItem(ownerId, itemId);
    }

    @GetMapping
    public List<ItemDtoBookingComments> getItemsByOwner(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemService.getItemsByOwner(ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        return itemService.searchItem(text);
    }
}