package src.main.java.ru.practicum.shareit.item.dto;

import lombok.*;
import src.main.java.ru.practicum.shareit.booking.dto.BookingDto;
import src.main.java.ru.practicum.shareit.comment.dto.CommentDto;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ItemDtoBookingComments {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long request;
    private BookingDto lastBooking;
    private BookingDto nextBooking;
    private List<CommentDto> comments;
}