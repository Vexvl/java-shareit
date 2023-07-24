package ru.practicum.shareit.Unit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.dto.ItemDto;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ItemTests {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRequestMapper itemRequestMapper;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Test
    public void testGetOwnerResponse() {
        Long userId = 1L;
        User requester = User.builder()
                .id(userId)
                .name("John")
                .email("john@example.com")
                .build();

        ItemRequest itemRequest1 = new ItemRequest();
        itemRequest1.setId(1L);
        itemRequest1.setCreated(LocalDateTime.now().minusDays(1));

        Item item1 = Item.builder()
                .id(1L)
                .name("Item 1")
                .build();

        ItemDto itemDto1 = ItemDto.builder()
                .id(1L)
                .name("Item 1")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findAllByRequester(requester)).thenReturn(Collections.singletonList(itemRequest1));
        when(itemRepository.findAllByRequest(itemRequest1.getId())).thenReturn(Collections.singletonList(item1));
        when(itemMapper.toItemDto(item1)).thenReturn(itemDto1);

        List<ItemRequestDto> resultDtos = itemRequestService.getOwnerResponse(userId);

        assertEquals(1, resultDtos.size());
    }

    @Test
    public void testGetAllNotOwner() {
        Long ownerId = 1L;
        Integer from = 0;
        Integer size = 10;

        if (from < 0) {
            assertThrows(IndexOutOfBoundsException.class, () -> itemRequestService.getAllNotOwner(ownerId, from, size));
            return;
        }

        User user = User.builder()
                .id(ownerId)
                .name("John")
                .email("john@example.com")
                .build();

        Pageable pageable = PageRequest.of(from / size, size);

        ItemRequest itemRequest1 = new ItemRequest();
        itemRequest1.setId(1L);
        itemRequest1.setCreated(LocalDateTime.now().minusDays(1));

        Item item1 = Item.builder()
                .id(1L)
                .name("Item 1")
                .build();

        ItemDto itemDto1 = ItemDto.builder()
                .id(1L)
                .name("Item 1")
                .build();

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllByRequesterNot(user, pageable)).thenReturn(Collections.singletonList(itemRequest1));
        when(itemRepository.findAllByRequest(itemRequest1.getId())).thenReturn(Collections.singletonList(item1));
        when(itemMapper.toItemDto(item1)).thenReturn(itemDto1);

        List<ItemRequestDto> resultDtos = itemRequestService.getAllNotOwner(ownerId, from, size);

        assertEquals(1, resultDtos.size());
        ItemRequestDto resultDto = resultDtos.get(0);

        verify(userRepository, times(1)).findById(ownerId);
        verify(itemRequestRepository, times(1)).findAllByRequesterNot(user, pageable);
        verify(itemRepository, times(1)).findAllByRequest(itemRequest1.getId());
        verify(itemMapper, times(1)).toItemDto(item1);
    }

    @Test
    public void testGetItemRequestById() {
        Long ownerId = 1L;
        Long requestId = 1L;

        User user = User.builder()
                .id(ownerId)
                .name("John")
                .email("john@example.com")
                .build();

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(requestId);
        itemRequest.setCreated(LocalDateTime.now().minusDays(1));

        Item item1 = Item.builder()
                .id(1L)
                .name("Item 1")
                .build();

        ItemDto itemDto1 = ItemDto.builder()
                .id(1L)
                .name("Item 1")
                .build();

        List<ItemDto> itemDtos = Collections.singletonList(itemDto1);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.findAllByRequest(itemRequest.getId())).thenReturn(Collections.singletonList(item1));
        when(itemMapper.toItemDto(item1)).thenReturn(itemDto1);
        when(itemRequestMapper.toItemRequestDto(itemRequest, itemDtos)).thenReturn(ItemRequestDto.builder()
                .id(itemRequest.getId())
                .created(itemRequest.getCreated())
                .items(itemDtos)
                .build());

        ItemRequestDto resultDto = itemRequestService.getItemRequestById(ownerId, requestId);

        assertEquals(itemRequest.getId(), resultDto.getId());
        assertEquals(itemDtos, resultDto.getItems());
        assertNotNull(resultDto.getCreated());

        verify(userRepository, times(1)).findById(ownerId);
        verify(itemRequestRepository, times(1)).findById(requestId);
        verify(itemRepository, times(1)).findAllByRequest(itemRequest.getId());
        verify(itemMapper, times(1)).toItemDto(item1);
        verify(itemRequestMapper, times(1)).toItemRequestDto(itemRequest, itemDtos);
    }
}