package ru.practicum.shareit.item.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.AbsenceException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;

    @Override
    public ItemDto addItem(Long ownerId, ItemDto itemDto) {
        //if (!userRepository.getAllUsers().containsKey(ownerId)) {
       //     throw new AbsenceException("User не существует");
       // }
        Item item = itemMapper.toItem(itemDto);
        return itemMapper.toItemDto(itemRepository.addItem(ownerId, item));
    }

    @Override
    public ItemDto editItem(Long ownerId, Long itemId, ItemDto itemDto) {
        Item item = itemMapper.toItem(itemDto);
        return itemMapper.toItemDto(itemRepository.editItem(ownerId, itemId, item));
    }

    @Override
    public ItemDto getItem(Long itemId, Long ownerId) {
        return itemMapper.toItemDto(itemRepository.getItem(itemId));
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long ownerId) {
        return itemRepository.getItemsByOwner(ownerId).stream().map(itemMapper::toItemDto).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItem(String text) {
        if (text.isEmpty()) {
            return List.of();
        }
        return itemRepository.searchItem(text).stream().map(itemMapper::toItemDto).collect(Collectors.toList());
    }
}