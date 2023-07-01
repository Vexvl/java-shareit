package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {

    Item addItem(Long ownerId, Item item);

    Item editItem(Long itemId, Long ownerId, Item item);

    Item getItem(Long itemId);

    List<Item> getItemsByOwner(Long ownerId);

    List<Item> searchItem(String text);
}