package ru.practicum.shareit.item.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBookingComments;
import ru.practicum.shareit.item.exception.AbsenceException;
import ru.practicum.shareit.item.exception.AccessDeniedException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public ItemDto addItem(Long ownerId, ItemDto itemDto) {
        User user = userRepository.findById(ownerId).orElseThrow(() -> new AbsenceException("User not exist"));
        Item item = itemRepository.save(itemMapper.toItem(itemDto, user));
        return itemMapper.toItemDto(item);
    }

    @Override
    public ItemDto editItem(Long ownerId, Long itemId, ItemDto itemDto) {
        User user = userRepository.findById(ownerId).orElseThrow(() -> new AbsenceException("User not exists"));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new AbsenceException("Item not exists"));
        Item newItem = itemMapper.toItem(itemDto, user);
        if (!Objects.equals(newItem.getOwner().getId(), ownerId)) {
            throw new AccessDeniedException("User не имеет права редактировать");
        }
        if (newItem.getAvailable() != null) {
            item.setAvailable(newItem.getAvailable());
        }
        if (newItem.getDescription() != null) {
            item.setDescription(newItem.getDescription());
        }
        if (newItem.getName() != null) {
            item.setName(newItem.getName());
        }
        itemRepository.save(item);
        return itemMapper.toItemDto(item);
    }

    @Override
    public ItemDtoBookingComments getItem(Long userId, Long itemId) {
        userRepository.findById(userId).orElseThrow(() -> new AbsenceException("User not exists"));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new AbsenceException("Item not exists"));
        ItemDtoBookingComments itemDtoBookingComments = itemMapper.toItemDtoBookingComments(item,
                commentRepository.findAllByItem(item));
        List<Booking> nextBookingsList = bookingRepository
                .findNextOrderedBookingsByItemId((itemDtoBookingComments.getId()));
        List<Booking> lastBookingsList = bookingRepository
                .findLastOrderedBookingsByItemId(itemDtoBookingComments.getId());
        if (!item.getOwner().getId().equals(userId)) {
            nextBookingsList = null;
            lastBookingsList = null;
        } else {
            if (!nextBookingsList.isEmpty()) {
                BookingDto nextBooking = bookingMapper.toBookingDto(nextBookingsList.get(0));
                itemDtoBookingComments.setNextBooking(nextBooking);
            }
            if (!lastBookingsList.isEmpty()) {
                BookingDto lastBooking = bookingMapper.toBookingDto(lastBookingsList.get(0));
                itemDtoBookingComments.setLastBooking(lastBooking);
            }
        }
        return itemDtoBookingComments;
    }

    @Override
    public List<ItemDtoBookingComments> getItemsByOwner(Long ownerId, Integer from, Integer size) {

        Pageable pageable = PageRequest.of(from / size, size);

        userRepository.findById(ownerId).orElseThrow(() -> new AbsenceException("User not exists"));

        List<ItemDtoBookingComments> itemDtoList = new ArrayList<>();
        List<Item> itemList = itemRepository.findByOwnerId(pageable, ownerId);

        List<Long> itemIds = itemList.stream().map(Item::getId).collect(Collectors.toList());

        List<Comment> allComments = commentRepository.findAllByItemIn(itemList);

        Map<Long, List<Comment>> commentsMap = allComments.stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));

        List<Booking> nextBookingsList = bookingRepository.findNextOrderedBookingsByItemIds(itemIds);
        List<Booking> lastBookingsList = bookingRepository.findLastOrderedBookingsByItemIds(itemIds);

        Map<Long, List<BookingDto>> nextBookingsMap = nextBookingsList.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId(),
                        Collectors.mapping(bookingMapper::toBookingDto, Collectors.toList())));

        Map<Long, List<BookingDto>> lastBookingsMap = lastBookingsList.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId(),
                        Collectors.mapping(bookingMapper::toBookingDto, Collectors.toList())));

        for (Item item : itemList) {
            Long itemId = item.getId();
            ItemDtoBookingComments itemDto = itemMapper.toItemDtoBookingComments(item,
                    commentsMap.getOrDefault(itemId, Collections.emptyList()));

            if (nextBookingsMap.containsKey(itemId)) {
                List<BookingDto> nextBookings = nextBookingsMap.get(itemId);
                BookingDto firstNextBooking = nextBookings.get(0);
                itemDto.setNextBooking(firstNextBooking);
            }

            if (lastBookingsMap.containsKey(itemId)) {
                List<BookingDto> lastBookings = lastBookingsMap.get(itemId);
                BookingDto firstLastBooking = lastBookings.get(0);
                itemDto.setLastBooking(firstLastBooking);
            }

            itemDtoList.add(itemDto);
        }
        return itemDtoList;
    }

    @Override
    public List<ItemDto> searchItem(Long ownerId, String text, Integer from, Integer size) {
        if (text.isEmpty()) {
            return List.of();
        }
        Pageable pageable = PageRequest.of(from / size, size);
        return itemRepository.searchByText(text, pageable).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}