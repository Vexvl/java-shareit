package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto addItemRequest(ItemRequestDto itemRequestDto, Long ownerId);

    List<ItemRequestDto> getOwnerResponse(Long ownerId);

    List<ItemRequestDto> getAllNotOwner(Long ownerId, Integer from, Integer size);

    ItemRequestDto getItemRequestById(Long ownerId, Long requestId);
}