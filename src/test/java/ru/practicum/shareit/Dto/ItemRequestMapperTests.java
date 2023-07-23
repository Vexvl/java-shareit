package ru.practicum.shareit.Dto;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ItemRequestMapperTests {

    @InjectMocks
    private ItemRequestMapper itemRequestMapper;

    @Test
    public void testToItemRequestDto() {
        User user = User.builder()
                .id(1L)
                .name("John")
                .email("john@example.com")
                .build();

        List<Item> items = Arrays.asList(
                Item.builder().id(1L).name("Item 1").description("Description 1").build(),
                Item.builder().id(2L).name("Item 2").description("Description 2").build()
        );

        ItemRequest itemRequest = ItemRequest.builder()
                .id(1L)
                .description("Item request description")
                .requester(user)
                .created(LocalDateTime.now())
                .build();

        List<ItemDto> itemsDto = Arrays.asList(
                ItemDto.builder().id(1L).name("Item 1").description("Description 1").build(),
                ItemDto.builder().id(2L).name("Item 2").description("Description 2").build()
        );

        ItemRequestDto itemRequestDto = itemRequestMapper.toItemRequestDto(itemRequest, itemsDto);

        assertEquals(1L, itemRequestDto.getId().longValue());
        assertEquals("Item request description", itemRequestDto.getDescription());
        assertEquals(1L, itemRequestDto.getRequester().longValue());
        assertEquals(itemsDto, itemRequestDto.getItems());
    }

    @Test
    public void testToItemRequest() {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("John")
                .email("john@example.com")
                .build();

        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Item request description")
                .requester(1L)
                .created(LocalDateTime.now())
                .build();

        User user = User.builder()
                .id(1L)
                .name("John")
                .email("john@example.com")
                .build();

        ItemRequest itemRequest = itemRequestMapper.toItemRequest(itemRequestDto, user);

        assertEquals(1L, itemRequest.getId().longValue());
        assertEquals("Item request description", itemRequest.getDescription());
        assertEquals(user, itemRequest.getRequester());
    }
}