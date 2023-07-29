package src.main.java.ru.practicum.shareit.booking.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import src.main.java.ru.practicum.shareit.booking.dto.BookingDto;
import src.main.java.ru.practicum.shareit.booking.model.Booking;
import src.main.java.ru.practicum.shareit.item.mapper.ItemMapper;
import src.main.java.ru.practicum.shareit.item.model.Item;
import src.main.java.ru.practicum.shareit.user.mapper.UserMapper;
import src.main.java.ru.practicum.shareit.user.model.User;

@Component
@AllArgsConstructor
public class BookingMapper {

    private final UserMapper userMapper;
    private final ItemMapper itemMapper;

    public BookingDto toBookingDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .itemId(booking.getItem().getId())
                .bookerId(booking.getBooker().getId())
                .booker(userMapper.toUserDto(booking.getBooker()))
                .item(itemMapper.toItemDto(booking.getItem()))
                .build();
    }

    public Booking toBooking(BookingDto bookingDto, Item item, User user) {
        return Booking.builder()
                .id(bookingDto.getId())
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .item(item)
                .booker(user)
                .status(bookingDto.getStatus())
                .build();
    }
}