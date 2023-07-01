package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto addItem(Long ownerId, ItemDto itemDto);

    ItemDto editItem(Long itemId, Long ownerId, ItemDto itemDto);

    ItemDto getItem(Long itemIdm, Long ownerId);

    List<ItemDto> getItemsByOwner(Long ownerId);

    List<ItemDto> searchItem(String text);
}