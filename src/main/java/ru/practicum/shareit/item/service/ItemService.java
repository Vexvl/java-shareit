package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBookingComments;

import java.util.List;

public interface ItemService {

    ItemDto addItem(Long ownerId, ItemDto itemDto);

    ItemDto editItem(Long ownerId, Long itemId, ItemDto itemDto);

    ItemDtoBookingComments getItem(Long ownerId, Long itemId);

    List<ItemDtoBookingComments> getItemsByOwner(Long ownerId, Integer from, Integer size);

    List<ItemDto> searchItem(Long ownerId, String text, Integer from, Integer size);
}