package ru.practicum.shareit.Dto;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BookingMapperTests {

    @Mock
    private UserMapper userMapper;

    @Mock
    private ItemMapper itemMapper;

    private BookingMapper bookingMapper;

    @Before
    public void setUp() {
        bookingMapper = new BookingMapper(userMapper, itemMapper);
    }

    @Test
    public void testToBookingDto() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusHours(2))
                .status(BookingStatus.APPROVED)
                .item(Item.builder().id(2L).name("Item 1").build())
                .booker(User.builder().id(3L).name("John").email("john@example.com").build())
                .build();

        UserDto userDto = UserDto.builder()
                .id(3L)
                .name("John")
                .email("john@example.com")
                .build();

        ItemDto itemDto = ItemDto.builder()
                .id(2L)
                .name("Item 1")
                .build();

        when(userMapper.toUserDto(any(User.class))).thenReturn(userDto);
        when(itemMapper.toItemDto(any(Item.class))).thenReturn(itemDto);

        BookingDto bookingDto = bookingMapper.toBookingDto(booking);

        assertEquals(1L, bookingDto.getId().longValue());
        assertEquals(booking.getStart(), bookingDto.getStart());
        assertEquals(booking.getEnd(), bookingDto.getEnd());
        assertEquals(BookingStatus.APPROVED, bookingDto.getStatus());
        assertEquals(2L, bookingDto.getItemId().longValue());
        assertEquals(3L, bookingDto.getBookerId().longValue());
        assertEquals(userDto, bookingDto.getBooker());
        assertEquals(itemDto, bookingDto.getItem());

        verify(userMapper, times(1)).toUserDto(booking.getBooker());
        verify(itemMapper, times(1)).toItemDto(booking.getItem());
    }

    @Test
    public void testToBooking() {
        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusHours(2))
                .status(BookingStatus.APPROVED)
                .itemId(2L)
                .bookerId(3L)
                .build();

        User user = User.builder()
                .id(3L)
                .name("John")
                .email("john@example.com")
                .build();

        Item item = Item.builder()
                .id(2L)
                .name("Item 1")
                .build();

        Booking booking = bookingMapper.toBooking(bookingDto, item, user);

        assertEquals(1L, booking.getId().longValue());
        assertEquals(bookingDto.getStart(), booking.getStart());
        assertEquals(bookingDto.getEnd(), booking.getEnd());
        assertEquals(BookingStatus.APPROVED, booking.getStatus());
        assertEquals(item, booking.getItem());
        assertEquals(user, booking.getBooker());
    }
}