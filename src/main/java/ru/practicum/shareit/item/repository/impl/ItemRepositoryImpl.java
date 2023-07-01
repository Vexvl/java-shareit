package ru.practicum.shareit.item.repository.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.exception.AccessDeniedException;
import ru.practicum.shareit.item.exception.ExistingException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;

import java.util.*;

@Component
@Slf4j
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, List<Item>> usersItemMap = new HashMap<>();
    private final Map<Long, Item> itemMap = new HashMap<>();
    private long id;

    @Override
    public Item addItem(Long ownerId, Item item) {
        if (ownerId != null){
            item.setOwnerId(ownerId);
        }
        id++;
        item.setId(id);
        itemMap.put(id,item);
        usersItemMap.computeIfAbsent(ownerId, k -> new ArrayList<>()).add(item);
        return item;
    }

    @Override
    public Item editItem(Long itemId, Long ownerId, Item item) {
        Item existingItem = itemMap.get(itemId);
        if (existingItem == null) {
            throw new ExistingException("Элемент не найден");
        }
        if (item.getAvailable() != null){
            existingItem.setAvailable(item.getAvailable());
        }
        if (item.getDescription() != null){
            existingItem.setDescription(item.getDescription());
        }
        if (item.getName() != null){
            existingItem.setName(item.getName());
        }
        return existingItem;
    }

    @Override
    public Item getItem(Long itemId) {
        return itemMap.get(itemId);
    }

    @Override
    public List<Item> getItemsByOwner(Long ownerId) {
        return usersItemMap.get(ownerId);
    }

    @Override
    public List<Item> searchItem(String text) {
        List<Item> searchResults = new ArrayList<>();
        for (Item item : itemMap.values()) {
            if (item.getName().toLowerCase().contains(text.toLowerCase()) ||
                    item.getDescription().toLowerCase().contains(text.toLowerCase())) {
                if (item.getAvailable()) {
                    searchResults.add(item);
                }
            }
        }
        return searchResults;
    }
}