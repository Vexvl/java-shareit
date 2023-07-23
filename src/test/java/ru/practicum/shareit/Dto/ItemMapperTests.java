package ru.practicum.shareit.Dto;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ItemMapperTests {

    @InjectMocks
    private ItemMapper itemMapper;

    @Test
    public void testToItemDto() {
        Item item = Item.builder()
                .id(1L)
                .name("Item 1")
                .description("Description 1")
                .available(true)
                .request(100L)
                .build();

        ItemDto itemDto = itemMapper.toItemDto(item);

        assertEquals(1L, itemDto.getId().longValue());
        assertEquals("Item 1", itemDto.getName());
        assertEquals("Description 1", itemDto.getDescription());
        assertEquals(true, itemDto.getAvailable());
        assertEquals(100L, itemDto.getRequestId().longValue());
    }

    @Test
    public void testToItem() {
        User user = User.builder()
                .id(1L)
                .name("John")
                .email("john@example.com")
                .build();

        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Item 1")
                .description("Description 1")
                .available(true)
                .requestId(100L)
                .build();

        Item item = itemMapper.toItem(itemDto, user);

        assertEquals(1L, item.getId().longValue());
        assertEquals("Item 1", item.getName());
        assertEquals("Description 1", item.getDescription());
        assertEquals(true, item.getAvailable());
        assertEquals(user, item.getOwner());
        assertEquals(100L, item.getRequest().longValue());
    }
}