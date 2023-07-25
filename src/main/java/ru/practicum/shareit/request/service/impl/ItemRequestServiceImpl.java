package ru.practicum.shareit.request.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.AbsenceException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;

    @Override
    @Transactional
    public ItemRequestDto addItemRequest(ItemRequestDto itemRequestDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AbsenceException("User not exists"));
        itemRequestDto.setCreated(LocalDateTime.now());
        ItemRequest itemRequest = itemRequestMapper.toItemRequest(itemRequestDto, user);
        return itemRequestMapper.toItemRequestDto(itemRequestRepository.save(itemRequest), List.of());
    }

    @Override
    @Transactional
    public List<ItemRequestDto> getOwnerResponse(Long userId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new AbsenceException("User not exists"));
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequester(requester);
        return itemRequests.stream()
                .map(itemRequest -> itemRequestMapper.toItemRequestDto(itemRequest,
                        itemRepository.findAllByRequest(itemRequest.getId())
                                .stream()
                                .map(itemMapper::toItemDto)
                                .collect(Collectors.toList())))
                .sorted(Comparator.comparing(ItemRequestDto::getCreated).reversed())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<ItemRequestDto> getAllNotOwner(Long ownerId, Integer from, Integer size) {
        if (from < 0) {
            throw new IndexOutOfBoundsException();
        }
        User user = userRepository.findById(ownerId)
                .orElseThrow(() -> new AbsenceException("User not exists"));
        Pageable pageable = PageRequest.of(from / size, size);
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterNot(user, pageable);
        return itemRequests.stream()
                .map(itemRequest -> itemRequestMapper.toItemRequestDto(itemRequest,
                        itemRepository.findAllByRequest(itemRequest.getId())
                                .stream()
                                .map(itemMapper::toItemDto)
                                .collect(Collectors.toList())))
                .sorted(Comparator.comparing(ItemRequestDto::getCreated).reversed())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemRequestDto getItemRequestById(Long ownerId, Long requestId) {
        userRepository.findById(ownerId).orElseThrow(() -> new AbsenceException("User not exists"));
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new AbsenceException("Request not exists"));

        List<ItemDto> itemDtos = itemRepository.findAllByRequest(itemRequest.getId())
                .stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());

        return itemRequestMapper.toItemRequestDto(itemRequest, itemDtos);
    }
}