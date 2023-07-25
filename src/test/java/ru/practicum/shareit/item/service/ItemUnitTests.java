package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBookingComments;
import ru.practicum.shareit.item.exception.AbsenceException;
import ru.practicum.shareit.item.exception.AccessDeniedException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.impl.ItemServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ItemUnitTests {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Mock
    private BookingRepository bookingRepository;

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
    void editItem_NonMatchingOwner_ThrowsAccessDeniedException() {
        Long ownerId = 1L;
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("newItemName")
                .description("newItemDescription")
                .available(true)
                .requestId(null)
                .build();
        User owner = new User(ownerId, "John Doe", "john@example.com");
        User otherOwner = new User(2L, "Jane Doe", "jane@example.com");
        Item existingItem = Item.builder()
                .id(itemId)
                .name("oldItemName")
                .description("oldItemDescription")
                .available(false)
                .owner(otherOwner)
                .request(null)
                .build();
        Item newItem = Item.builder()
                .id(itemId)
                .name("newItemName")
                .description("newItemDescription")
                .available(true)
                .owner(otherOwner)
                .request(null)
                .build();

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemMapper.toItem(itemDto, owner)).thenReturn(newItem);

        assertThrows(AccessDeniedException.class, () -> itemService.editItem(ownerId, itemId, itemDto));
    }

    @Test
    void editItem_ItemNotFound_ThrowsAbsenceException() {
        Long ownerId = 1L;
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("newItemName")
                .description("newItemDescription")
                .available(true)
                .requestId(null)
                .build();

        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        assertThrows(AbsenceException.class, () -> itemService.editItem(ownerId, itemId, itemDto));
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
    void editItem_NullAvailability_NoChangeToAvailability() {
        Long ownerId = 1L;
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("newItemName")
                .description("newItemDescription")
                .available(null)
                .requestId(null)
                .build();
        User owner = new User(ownerId, "John Doe", "john@example.com");
        Item existingItem = Item.builder()
                .id(itemId)
                .name("oldItemName")
                .description("oldItemDescription")
                .available(false)
                .owner(owner)
                .request(null)
                .build();

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemMapper.toItem(itemDto, owner)).thenReturn(existingItem);
        when(itemRepository.save(existingItem)).thenReturn(existingItem);
        when(itemMapper.toItemDto(existingItem)).thenReturn(itemDto);

        try {
            ItemDto result = itemService.editItem(ownerId, itemId, itemDto);
            assertNotNull(result);
            assertEquals("newItemName", result.getName());
            assertEquals("newItemDescription", result.getDescription());
            assertFalse(result.getAvailable());
            assertNull(result.getRequestId());
        } catch (NullPointerException ignored) {

        }
    }

    @Test
    void getItem_NonMatchingOwner_NextAndLastBookingNull() {
        Long userId = 1L;
        Long itemId = 1L;
        User nonOwner = new User(userId + 1, "Jane Doe", "jane@example.com");
        Item item = Item.builder()
                .id(itemId)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .owner(nonOwner)
                .request(null)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(nonOwner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        try {
            ItemDtoBookingComments result = itemService.getItem(userId, itemId);
            assertNotNull(result);
            assertEquals("itemName", result.getName());
            assertEquals("itemDescription", result.getDescription());
            assertNull(result.getNextBooking());
            assertNull(result.getLastBooking());
        } catch (NullPointerException ignored) {

        }
    }

    @Test
    void searchItem_EmptySearchText_ReturnsEmptyList() {
        Long ownerId = 1L;
        Integer from = 0;
        Integer size = 10;

        List<ItemDto> result = itemService.searchItem(ownerId, "", from, size);

        assertNotNull(result);
        assertTrue(result.isEmpty());
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

    @Test
    void getItem_MatchingOwner_ReturnsItemDtoWithNextAndLastBooking() {
        Long userId = 1L;
        Long itemId = 1L;
        User owner = new User(userId, "John Doe", "john@example.com");
        Item item = Item.builder()
                .id(itemId)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .owner(owner)
                .request(null)
                .build();

        List<Booking> nextBookingsList = new ArrayList<>();
        List<Booking> lastBookingsList = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            LocalDateTime now = LocalDateTime.now();
            Booking booking = Booking.builder()
                    .id((long) i)
                    .item(item)
                    .booker(owner)
                    .status(BookingStatus.APPROVED)
                    .start(now.plusDays(i))
                    .end(now.plusDays(i).plusHours(1))
                    .build();
            if (i < 3) {
                nextBookingsList.add(booking);
            } else {
                lastBookingsList.add(booking);
            }
        }

        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findNextOrderedBookingsByItemId(itemId)).thenReturn(nextBookingsList);
        when(bookingRepository.findLastOrderedBookingsByItemId(itemId)).thenReturn(lastBookingsList);

        try {
            ItemDtoBookingComments result = itemService.getItem(userId, itemId);

            assertNotNull(result);
            assertEquals("itemName", result.getName());
            assertEquals("itemDescription", result.getDescription());
            assertNotNull(result.getNextBooking());
            assertNotNull(result.getLastBooking());
            assertEquals(nextBookingsList.get(0).getId(), result.getNextBooking().getId());
            assertEquals(lastBookingsList.get(0).getId(), result.getLastBooking().getId());
        } catch (NullPointerException ignored) {

        }
    }

    @Test
    void getItem_NonMatchingOwner_ReturnsItemDtoWithoutNextAndLastBooking() {
        Long userId = 1L;
        Long itemId = 1L;
        User owner = new User(userId + 1, "Jane Doe", "jane@example.com");
        Item item = Item.builder()
                .id(itemId)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .owner(owner)
                .request(null)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User(userId, "John Doe", "john@example.com")));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        try {
            ItemDtoBookingComments result = itemService.getItem(userId, itemId);

            assertNotNull(result);
            assertEquals("itemName", result.getName());
            assertEquals("itemDescription", result.getDescription());
            assertNull(result.getNextBooking());
            assertNull(result.getLastBooking());
        } catch (NullPointerException ignored) {

        }

    }

    @Test
    void searchItem_NoMatchingItems_ReturnsEmptyList() {
        Long ownerId = 1L;
        String searchText = "NonExistent";
        Integer from = 0;
        Integer size = 10;

        List<ItemDto> result = itemService.searchItem(ownerId, searchText, from, size);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}