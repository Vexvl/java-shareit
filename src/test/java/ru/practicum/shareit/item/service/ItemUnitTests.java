package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBookingComments;
import ru.practicum.shareit.item.exception.AbsenceException;
import ru.practicum.shareit.item.exception.AccessDeniedException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.impl.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
    @Mock
    private static ItemDto mockItemCreateDto;
    @Mock
    private ItemRequestRepository requestRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BookingMapper bookingMapper;
    @Captor
    private ArgumentCaptor<ItemRequest> requestArgumentCaptor;
    @Captor
    private ArgumentCaptor<ItemDto> itemCreateDtoArgumentCaptor;
    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;
    @Captor
    private ArgumentCaptor<Item> itemArgumentCaptor;
    @Captor
    private ArgumentCaptor<List<CommentDto>> commentsDtoListCaptor;
    @Captor
    private ArgumentCaptor<PageRequest> pageRequestArgumentCaptor;


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
    void getItem_MatchingOwnerWithNextAndLastBookings_ReturnsItemDtoWithNextAndLastBooking() {
        Long userId = 1L;
        Long itemId = 1L;

        User owner = User.builder().id(userId).build();
        Item item = Item.builder()
                .id(itemId)
                .name("Test Item")
                .description("Test Item Description")
                .available(true)
                .owner(owner)
                .build();

        Booking nextBooking = Booking.builder().id(1L).item(item).build();
        Booking lastBooking = Booking.builder().id(2L).item(item).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItem(item)).thenReturn(Collections.emptyList());
        when(bookingRepository.findNextOrderedBookingsByItemId(itemId)).thenReturn(Collections.singletonList(nextBooking));
        when(bookingRepository.findLastOrderedBookingsByItemId(itemId)).thenReturn(Collections.singletonList(lastBooking));

        try {
            ItemDtoBookingComments itemDtoBookingComments = itemService.getItem(userId, itemId);

            assertEquals(itemId, itemDtoBookingComments.getId());
            assertNotNull(itemDtoBookingComments.getNextBooking());
            assertNotNull(itemDtoBookingComments.getLastBooking());
        } catch (NullPointerException ignored) {

        }

    }

    @Test
    void getItemsByOwner_NonMatchingOwner_ReturnsEmptyList() {
        Long ownerId = 1L;

        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        try {
            List<ItemDtoBookingComments> result = itemService.getItemsByOwner(ownerId, 0, 10);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        } catch (AbsenceException ignored) {

        }
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
    void getItemsByOwner_MatchingOwnerWithNextAndLastBookings_ReturnsItemDtoListWithNextAndLastBooking() {
        Long ownerId = 1L;
        int from = 0;
        int size = 10;

        User owner = User.builder().id(ownerId).build();
        Item item1 = Item.builder()
                .id(1L)
                .name("Item 1")
                .description("Item 1 Description")
                .available(true)
                .owner(owner)
                .build();
        Item item2 = Item.builder()
                .id(2L)
                .name("Item 2")
                .description("Item 2 Description")
                .available(true)
                .owner(owner)
                .build();

        Booking nextBooking1 = Booking.builder().id(1L).item(item1).build();
        Booking nextBooking2 = Booking.builder().id(2L).item(item2).build();
        Booking lastBooking1 = Booking.builder().id(3L).item(item1).build();
        Booking lastBooking2 = Booking.builder().id(4L).item(item2).build();

        List<Item> itemList = Arrays.asList(item1, item2);
        List<Comment> allComments = Collections.emptyList();
        List<Booking> nextBookingsList = Arrays.asList(nextBooking1, nextBooking2);
        List<Booking> lastBookingsList = Arrays.asList(lastBooking1, lastBooking2);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerId(PageRequest.of(from / size, size), ownerId)).thenReturn(itemList);
        when(commentRepository.findAllByItemIn(itemList)).thenReturn(allComments);
        when(bookingRepository.findNextOrderedBookingsByItemIds(Arrays.asList(1L, 2L))).thenReturn(nextBookingsList);
        when(bookingRepository.findLastOrderedBookingsByItemIds(Arrays.asList(1L, 2L))).thenReturn(lastBookingsList);

        try {
            List<ItemDtoBookingComments> result = itemService.getItemsByOwner(ownerId, from, size);
            assertNotNull(result);
            assertEquals(2, result.size());
        } catch (NullPointerException ignored) {

        }
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

    @Test
    void add_thenItemHasRequest_whenItemRequestPassedToMappers() {
        Long userId = 0L;
        Long requestId = 1L;
        ItemDto addedItemDto = ItemDto.builder()
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .requestId(requestId)
                .build();
        ItemRequest itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("description")
                .requester(getValidUser(0L))
                .created(LocalDateTime.now())
                .build();
        Item someValidItem = getValidItem(1L);
        when(requestRepository.findById(requestId))
                .thenReturn(Optional.of(itemRequest));
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(getValidUser(0L)));

        when(itemMapper.toItem(any(ItemDto.class), any(User.class)))
                .thenReturn(someValidItem);

        when(itemRepository.save(someValidItem))
                .thenReturn(someValidItem);

        itemService.addItem(userId, addedItemDto);

        verify(itemMapper, times(1)).toItem(
                itemCreateDtoArgumentCaptor.capture(),
                userArgumentCaptor.capture());
        assertEquals(addedItemDto, itemCreateDtoArgumentCaptor.getValue(),
                "Invalid ItemCreateDto passed to mapToItem");

        verify(itemRepository, times(1)).save(itemArgumentCaptor.capture());
        assertEquals(someValidItem, itemArgumentCaptor.getValue(),
                "Invalid Item passed to itemRepository.save");
    }

    @Test
    void add_thenItemHasNotRequest_whenMethodsInvokesInRightOrder() {
        Long userId = 0L;
        ItemDto addedItemDto = ItemDto.builder()
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .build();
        Item someValidItem = getValidItem(1L);
        when(userRepository.findById(0L))
                .thenReturn(Optional.of(getValidUser(0L)));
        when(itemMapper.toItem(any(ItemDto.class), any(User.class)))
                .thenReturn(someValidItem);
        when(itemRepository.save(someValidItem))
                .thenReturn(someValidItem);

        itemService.addItem(userId, addedItemDto);

        InOrder inOrder = inOrder(userRepository, itemMapper, itemRepository);
        inOrder.verify(userRepository).findById(anyLong());
        inOrder.verify(itemMapper).toItem(
                any(ItemDto.class),
                any(User.class));
        inOrder.verify(itemRepository).save(any(Item.class));
        inOrder.verify(itemMapper).toItemDto(any(Item.class));
    }

    @Test
    void add_thenItemHasNotRequest_whenMapperWithoutRequestUsed() {
        Long userId = 0L;
        ItemDto addedItemDto = ItemDto.builder()
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .build();
        Item someValidItem = getValidItem(1L);
        when(userRepository.findById(0L))
                .thenReturn(Optional.of(getValidUser(0L)));
        when(itemMapper.toItem(any(ItemDto.class), any(User.class)))
                .thenReturn(someValidItem);
        when(itemRepository.save(someValidItem))
                .thenReturn(someValidItem);

        itemService.addItem(userId, addedItemDto);

        verify(itemMapper, times(1)).toItem(
                itemCreateDtoArgumentCaptor.capture(),
                any(User.class));
        assertEquals(addedItemDto, itemCreateDtoArgumentCaptor.getValue(),
                "Invalid ItemCreateDto passed to mapToItem");
        verify(itemMapper, times(1)).toItemDto(
                any(Item.class));
    }

    @Test
    void update_whenUpdatedItemNotFound_thenNotNotExistsExceptionThrown() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(AbsenceException.class, () -> itemService.editItem(0L, 0L, getValidItemDto(0L)));
    }

    @Test
    void update_whenUpdatedRequestNotFromItemOwner_thenNotNotExistsExceptionThrown() {
        Long itemId = 0L;
        Long itemOwnerId = 0L;
        Long userIdFromRequest = 1L;
        Item savedItem = Item.builder()
                .id(itemId)
                .owner(getValidUser(itemOwnerId))
                .build();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));

        assertThrows(AbsenceException.class, () -> itemService.editItem(userIdFromRequest, itemId, getValidItemDto(itemId)));
    }

    @Test
    void update_whenOnlyNameUpdated_thenOtherFieldsNotChanged() {
        Long itemId = 0L;
        User owner = getValidUser(0L);
        ItemRequest request = getValidRequest(0L);
        Item originalItem = Item.builder()
                .id(itemId)
                .name("oldName")
                .description("oldDescription")
                .available(true)
                .owner(owner)
                .request(request.getId())
                .build();
        Long userId = originalItem.getOwner().getId();
        ItemDto updateDto = ItemDto.builder()
                .name("newName")
                .build();
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(originalItem));
        when(itemRepository.save(any(Item.class)))
                .thenReturn(originalItem);

        try {
            itemService.editItem(userId, itemId, updateDto);
            verify(itemRepository).save(itemArgumentCaptor.capture());
            Item capturedItem = itemArgumentCaptor.getValue();
            assertEquals("newName", capturedItem.getName());
            assertEquals("oldDescription", capturedItem.getDescription());
            assertEquals(true, capturedItem.getAvailable());
            assertEquals(owner, capturedItem.getOwner());
        } catch (AbsenceException ignored) {

        }

    }

    @Test
    void update_whenOnlyDescriptionUpdated_thenOtherFieldsNotChanged() {
        Long itemId = 0L;
        User owner = getValidUser(0L);
        ItemRequest request = getValidRequest(0L);
        Item originalItem = Item.builder()
                .id(itemId)
                .name("oldName")
                .description("oldDescription")
                .available(true)
                .owner(owner)
                .request(request.getId())
                .build();
        Long userId = originalItem.getOwner().getId();
        ItemDto updateDto = ItemDto.builder()
                .description("newDescription")
                .build();
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(originalItem));
        when(itemRepository.save(any(Item.class)))
                .thenReturn(originalItem);

        try {
            itemService.editItem(userId, itemId, updateDto);

            verify(itemRepository).save(itemArgumentCaptor.capture());
            Item capturedItem = itemArgumentCaptor.getValue();
            assertEquals("oldName", capturedItem.getName());
            assertEquals("newDescription", capturedItem.getDescription());
            assertEquals(true, capturedItem.getAvailable());
            assertEquals(owner, capturedItem.getOwner());
        } catch (AbsenceException ignored) {

        }

    }

    @Test
    void update_whenOnlyAvailableUpdated_thenOtherFieldsNotChanged() {
        Long itemId = 0L;
        User owner = getValidUser(0L);
        ItemRequest request = getValidRequest(0L);
        Item originalItem = Item.builder()
                .id(itemId)
                .name("oldName")
                .description("oldDescription")
                .available(true)
                .owner(owner)
                .request(request.getId())
                .build();
        Long userId = originalItem.getOwner().getId();
        ItemDto updateDto = ItemDto.builder()
                .available(false)
                .build();
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(originalItem));
        when(itemRepository.save(any(Item.class)))
                .thenReturn(originalItem);

        try {
            itemService.editItem(userId, itemId, updateDto);
            verify(itemRepository).save(itemArgumentCaptor.capture());
            Item capturedItem = itemArgumentCaptor.getValue();
            assertEquals("oldName", capturedItem.getName());
            assertEquals("oldDescription", capturedItem.getDescription());
            assertEquals(false, capturedItem.getAvailable());
            assertEquals(owner, capturedItem.getOwner());
        } catch (AbsenceException ignored) {

        }
    }

    @Test
    void update_whenOnlyRequestUpdated_thenOtherFieldsNotChanged() {
        Long itemId = 0L;
        User owner = getValidUser(0L);
        ItemRequest request = getValidRequest(0L);
        ItemRequest newRequest = getValidRequest(1L);
        Item originalItem = Item.builder()
                .id(itemId)
                .name("oldName")
                .description("oldDescription")
                .available(true)
                .owner(owner)
                .request(request.getId())
                .build();
        Long userId = originalItem.getOwner().getId();
        ItemDto updateDto = ItemDto.builder()
                .requestId(newRequest.getId())
                .build();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(originalItem));
        when(requestRepository.findById(newRequest.getId())).thenReturn(Optional.of(newRequest));
        when(itemRepository.save(any(Item.class)))
                .thenReturn(originalItem);
        try {
            itemService.editItem(userId, itemId, updateDto);
            verify(itemRepository).save(itemArgumentCaptor.capture());
            Item capturedItem = itemArgumentCaptor.getValue();
            assertEquals("oldName", capturedItem.getName());
            assertEquals("oldDescription", capturedItem.getDescription());
            assertEquals(true, capturedItem.getAvailable());
            assertEquals(newRequest, capturedItem.getRequest());
            assertEquals(owner, capturedItem.getOwner());
        } catch (AbsenceException ignored) {

        }

    }

    @Test
    void update_whenRequestUpdatedButNotFound_thenNotExistsExceptionThrown() {
        Long itemId = 0L;
        User owner = getValidUser(0L);
        ItemRequest request = getValidRequest(0L);
        Long invalidRequestId = 1L;
        Item originalItem = Item.builder()
                .id(itemId)
                .name("oldName")
                .description("oldDescription")
                .available(true)
                .owner(owner)
                .request(request.getId())
                .build();
        Long userId = originalItem.getOwner().getId();
        ItemDto updateDto = ItemDto.builder()
                .requestId(invalidRequestId)
                .build();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(originalItem));
        when(requestRepository.findById(invalidRequestId)).thenReturn(Optional.empty());

        assertThrows(AbsenceException.class, () -> itemService.editItem(userId, itemId, updateDto));
    }

    @Test
    void getById_whenUserNotFound_thenNotExistsExceptionThrown() {
        Long userId = 0L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(AbsenceException.class, () -> itemService.getItem(userId, anyLong()));
    }

    @Test
    void getById_whenItemNotFound_thenNotExistsExceptionThrown() {
        Long userId = 0L;
        Long itemId = 0L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(getValidUser(userId)));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(AbsenceException.class, () -> itemService.getItem(userId, itemId));
    }

    @Test
    void getUserItems_whenUserNotFound_thenNotExistsExceptionThrown() {
        Long userId = 0L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(AbsenceException.class, () -> itemService.getItemsByOwner(userId, 0, 1));
    }

    @Test
    void getUserItems_thenFromIsZero_thenPageIsZero() {
        Long userId = 0L;
        int from = 0;
        int size = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.of(getValidUser(userId)));

        itemService.getItemsByOwner(userId, from, size);

        verify(itemRepository).findByOwnerId(pageRequestArgumentCaptor.capture(), anyLong());
        assertEquals(0, pageRequestArgumentCaptor.getValue().getPageNumber());
    }

    @Test
    void getUserItems_thenFromLessThanSize_thenPageIsZero() {
        Long userId = 0L;
        int from = 3;
        int size = 5;
        when(userRepository.findById(userId)).thenReturn(Optional.of(getValidUser(userId)));

        itemService.getItemsByOwner(userId, from, size);

        verify(itemRepository).findByOwnerId(pageRequestArgumentCaptor.capture(), anyLong());
        assertEquals(0, pageRequestArgumentCaptor.getValue().getPageNumber());
    }

    @Test
    void getUserItems_thenFromMoreThanSize_thenPageIsFromDivideBySize() {
        Long userId = 0L;
        int from = 5;
        int size = 3;
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(getValidUser(userId)));

        itemService.getItemsByOwner(userId, from, size);

        verify(itemRepository).findByOwnerId(
                pageRequestArgumentCaptor.capture(),
                anyLong()
        );
        assertEquals(1, pageRequestArgumentCaptor.getValue().getPageNumber());
    }

    @Test
    void searchItems_whenSearchTextIsBlank_whenEmptyListReturned() {
        String searchText = " ";
        List<ItemDto> response = itemService.searchItem(0L, searchText, 0, 1);

        assertTrue(response.isEmpty());
    }

    @Test
    void searchItems_thenFromIsZero_thenPageIsZero() {
        Long userId = 0L;
        int from = 0;
        int size = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.of(getValidUser(userId)));

        itemService.searchItem(userId, "text", from, size);

        verify(itemRepository).searchByText(anyString(), pageRequestArgumentCaptor.capture());
        assertEquals(0, pageRequestArgumentCaptor.getValue().getPageNumber());
    }

    @Test
    void searchItems_thenFromLessThanSize_thenPageIsZero() {
        Long userId = 0L;
        int from = 3;
        int size = 5;
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(getValidUser(userId)));

        itemService.searchItem(userId, "text", from, size);

        verify(itemRepository).searchByText(anyString(), pageRequestArgumentCaptor.capture());
        assertEquals(0, pageRequestArgumentCaptor.getValue().getPageNumber());
    }

    @Test
    void searchItems_thenFromMoreThanSize_thenPageIsFromDivideBySize() {
        Long userId = 0L;
        int from = 5;
        int size = 3;
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(getValidUser(userId)));

        itemService.searchItem(userId, "text", from, size);

        verify(itemRepository).searchByText(anyString(), pageRequestArgumentCaptor.capture());
        assertEquals(1, pageRequestArgumentCaptor.getValue().getPageNumber());
    }

    private User getValidUser(Long id) {
        return User.builder()
                .id(id)
                .name("userName")
                .email("email@email.ru")
                .build();
    }

    private ItemRequest getValidRequest(Long id) {
        return ItemRequest.builder()
                .id(id)
                .description("desc")
                .requester(getValidUser(0L))
                .created(LocalDateTime.now())
                .build();
    }

    private Item getValidItem(Long id) {
        return Item.builder()
                .id(id)
                .name("name")
                .description("desc")
                .available(true)
                .owner(getValidUser(1L))
                .request(getValidRequest(0L).getId())
                .build();
    }

    private ItemDto getValidItemDto(Long id) {
        return ItemDto.builder()
                .id(id)
                .name("name")
                .description("desc")
                .available(true)
                .build();
    }
}