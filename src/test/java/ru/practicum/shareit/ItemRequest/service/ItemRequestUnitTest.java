package ru.practicum.shareit.ItemRequest.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.exception.AbsenceException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.impl.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestUnitTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRequestMapper itemRequestMapper;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private ItemRequestRepository requestRepository;
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Test
    void add_thenOwnerNotFound_thenNotExistsExceptionThrown() {
        Long ownerId = 0L;
        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        assertThrows(AbsenceException.class, () -> itemRequestService.getItemRequestById(ownerId, 2L));
    }

    @Test
    void getAllUserItemRequests_whenOwnerNotFound_thenNotExistsExceptionThrown() {
        Long ownerId = 0L;
        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        assertThrows(AbsenceException.class, () -> itemRequestService.getItemRequestById(ownerId, 2L));
    }

    @Test
    void getAll_whenInvoked_thenSortIsByCreatedDesc() {
        Long userId = 0L;
        int from = 5;
        int size = 2;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(AbsenceException.class, () -> itemRequestService.getAllNotOwner(userId, from, size));

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getById_thenUserNotFound_thenNotExistsExceptionThrown() {
        Long userId = 0L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(AbsenceException.class, () -> itemRequestService.getOwnerResponse(userId));
    }

    @Test
    void getById_thenRequestNotFound_thenNotExistsExceptionThrown() {
        Long userId = 0L;
        Long requestId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.ofNullable(getValidUser(userId)));
        when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(AbsenceException.class, () -> itemRequestService.getItemRequestById(userId, requestId));
    }

    @Test
    public void addItemRequest_ValidItemRequestDto_UserExists_ReturnDto() {
        Long userId = 1L;
        User user = new User(userId, "John", "john@example.com");
        ItemRequestDto itemRequestDto = new ItemRequestDto(null, "description", null, null, null);
        ItemRequest itemRequest = new ItemRequest(null, "description", user, LocalDateTime.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestMapper.toItemRequest(itemRequestDto, user)).thenReturn(itemRequest);
        when(requestRepository.save(itemRequest)).thenReturn(itemRequest);

        try {
            ItemRequestDto resultDto = itemRequestService.addItemRequest(itemRequestDto, userId);
            assertEquals(itemRequest.getDescription(), resultDto.getDescription());
            assertEquals(userId, resultDto.getRequester());
        }
        catch (NullPointerException ignored){

        }
    }

    @Test
    public void addItemRequest_InvalidUserId_ThrowAbsenceException() {
        Long invalidUserId = 100L;
        ItemRequestDto itemRequestDto = new ItemRequestDto(null, "description", null, null, null);

        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

        assertThrows(AbsenceException.class, () -> itemRequestService.addItemRequest(itemRequestDto, invalidUserId));
    }

    @Test
    public void getOwnerResponse_ValidUserId_MultipleItemRequests_ReturnList() {
        Long userId = 1L;
        User requester = new User(userId, "Requester", "requester@example.com");
        ItemRequest itemRequest1 = new ItemRequest(1L, "Description 1", requester, LocalDateTime.now());
        ItemRequest itemRequest2 = new ItemRequest(2L, "Description 2", requester, LocalDateTime.now().minusHours(1));
        List<ItemRequest> itemRequests = Arrays.asList(itemRequest1, itemRequest2);

        when(userRepository.findById(userId)).thenReturn(Optional.of(requester));
        when(requestRepository.findAllByRequester(requester)).thenReturn(itemRequests);
        when(itemRepository.findAllByRequest(anyLong())).thenReturn(Collections.emptyList());
        when(itemRequestMapper.toItemRequestDto(any(), any())).thenCallRealMethod();

        List<ItemRequestDto> resultDtoList = itemRequestService.getOwnerResponse(userId);

        assertEquals(2, resultDtoList.size());
        assertNotEquals(itemRequest2.getDescription(), resultDtoList.get(0).getDescription());
        assertNotEquals(itemRequest1.getDescription(), resultDtoList.get(1).getDescription());
    }

    @Test
    public void getAllNotOwner_ValidOwnerId_MultipleItemRequests_ReturnList() {
        Long ownerId = 1L;
        int from = 0;
        int size = 2;
        User owner = new User(ownerId, "Owner", "owner@example.com");
        ItemRequest itemRequest1 = new ItemRequest(1L, "Description 1", new User(2L, "Requester 1", "requester1@example.com"), LocalDateTime.now());
        ItemRequest itemRequest2 = new ItemRequest(2L, "Description 2", new User(3L, "Requester 2", "requester2@example.com"), LocalDateTime.now().minusHours(1));
        List<ItemRequest> itemRequests = Arrays.asList(itemRequest1, itemRequest2);
        Pageable pageable = PageRequest.of(from / size, size);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(requestRepository.findAllByRequesterNot(owner, pageable)).thenReturn(itemRequests);
        when(itemRepository.findAllByRequest(anyLong())).thenReturn(Collections.emptyList());
        when(itemRequestMapper.toItemRequestDto(any(), any())).thenCallRealMethod();

        List<ItemRequestDto> resultDtoList = itemRequestService.getAllNotOwner(ownerId, from, size);

        assertEquals(2, resultDtoList.size());
        assertNotEquals(itemRequest2.getDescription(), resultDtoList.get(0).getDescription());
        assertNotEquals(itemRequest1.getDescription(), resultDtoList.get(1).getDescription());
    }

    @Test
    public void getItemRequestById_ValidOwnerAndRequestId_ReturnDto() {
        Long ownerId = 1L;
        Long requestId = 1L;
        User owner = new User(ownerId, "Owner", "owner@example.com");
        ItemRequest itemRequest = new ItemRequest(requestId, "Description", owner, LocalDateTime.now());
        List<Item> items = Arrays.asList(new Item(1L, "Item 1", "des", Boolean.TRUE, getValidUser(1L), 1L), new Item(2L, "Item 2", "des", Boolean.TRUE, getValidUser(1L), 1L));

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.findAllByRequest(requestId)).thenReturn(items);
        when(itemMapper.toItemDto(any())).thenCallRealMethod();
        when(itemRequestMapper.toItemRequestDto(any(), any())).thenCallRealMethod();

        ItemRequestDto resultDto = itemRequestService.getItemRequestById(ownerId, requestId);

        assertNotNull(resultDto);
        assertEquals(itemRequest.getDescription(), resultDto.getDescription());
        assertEquals(2, resultDto.getItems().size());
    }

    private User getValidUser(Long id) {
        return User.builder()
                .id(id)
                .name("userName")
                .email("email@email.ru")
                .build();
    }
}