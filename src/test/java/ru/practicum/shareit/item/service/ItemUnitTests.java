package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBookingComments;
import ru.practicum.shareit.item.exception.AbsenceException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.impl.ItemServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ItemUnitTests {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void addItem_ValidOwnerAndItemDto_ItemAddedSuccessfully() {
        Long ownerId = 1L;
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .requestId(null)
                .build();
        User owner = new User(ownerId, "John Doe", "john@example.com");
        Item item = Item.builder()
                .id(itemId)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .owner(owner)
                .request(null)
                .build();

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemMapper.toItem(itemDto, owner)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.addItem(ownerId, itemDto);

        assertNotNull(result);
        assertEquals(itemDto.getName(), result.getName());
        assertEquals(itemDto.getDescription(), result.getDescription());
        assertEquals(itemDto.getAvailable(), result.getAvailable());
        assertNull(result.getRequestId());
    }

    @Test
    void editItem_ValidOwnerAndItemDto_ItemEditedSuccessfully() {
        Long ownerId = 1L;
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("newItemName")
                .description("newItemDescription")
                .available(false)
                .requestId(null)
                .build();
        User owner = new User(ownerId, "John Doe", "john@example.com");
        Item existingItem = Item.builder()
                .id(itemId)
                .name("oldItemName")
                .description("oldItemDescription")
                .available(true)
                .owner(owner)
                .request(null)
                .build();

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemMapper.toItem(itemDto, owner)).thenReturn(existingItem);
        when(itemRepository.save(existingItem)).thenReturn(existingItem);
        when(itemMapper.toItemDto(existingItem)).thenReturn(itemDto);

        ItemDto result = itemService.editItem(ownerId, itemId, itemDto);

        assertNotNull(result);
        assertEquals(itemDto.getName(), result.getName());
        assertEquals(itemDto.getDescription(), result.getDescription());
        assertEquals(itemDto.getAvailable(), result.getAvailable());
        assertNull(result.getRequestId());
    }

    @Test
    void addItem_OwnerNotFound_ThrowsAbsenceException() {
        Long ownerId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .requestId(null)
                .build();

        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        assertThrows(AbsenceException.class, () -> itemService.addItem(ownerId, itemDto));
    }
    @Test
    void editItem_OwnerNotFound_ThrowsAbsenceException() {
        Long ownerId = 1L;
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("newItemName")
                .description("newItemDescription")
                .available(false)
                .requestId(null)
                .build();

        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        assertThrows(AbsenceException.class, () -> itemService.editItem(ownerId, itemId, itemDto));
    }

    @Test
    void editItem_ItemNotFound_ThrowsAbsenceException() {
        Long ownerId = 1L;
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("newItemName")
                .description("newItemDescription")
                .available(false)
                .requestId(null)
                .build();
        User owner = new User(ownerId, "John Doe", "john@example.com");

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(AbsenceException.class, () -> itemService.editItem(ownerId, itemId, itemDto));
    }

    @Test
    void getItem_ValidUserAndItemId_ReturnsItemDtoBookingComments() {
        Long userId = 1L;
        Long itemId = 1L;
        User user = new User(userId, "John Doe", "john@example.com");
        Item item = Item.builder()
                .id(itemId)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .owner(user)
                .request(null)
                .build();
        List<Comment> comments = Collections.singletonList(new Comment());
        ItemDtoBookingComments expectedResponse = new ItemDtoBookingComments();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItem(item)).thenReturn(comments);
        when(itemMapper.toItemDtoBookingComments(item, comments)).thenReturn(expectedResponse);

        ItemDtoBookingComments result = itemService.getItem(userId, itemId);

        assertEquals(expectedResponse, result);
    }

    @Test
    void getItem_InvalidUserId_ThrowsAbsenceException() {
        Long userId = 1L;
        Long itemId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(AbsenceException.class, () -> itemService.getItem(userId, itemId));
    }

    @Test
    void getItem_ItemNotFound_ThrowsAbsenceException() {
        Long userId = 1L;
        Long itemId = 1L;
        User user = new User(userId, "John Doe", "john@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(AbsenceException.class, () -> itemService.getItem(userId, itemId));
    }

    @Test
    void searchItem_EmptyText_ReturnsEmptyList() {
        Long ownerId = 1L;
        String text = "";
        Integer from = 0;
        Integer size = 10;

        List<ItemDto> result = itemService.searchItem(ownerId, text, from, size);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}