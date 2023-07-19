package ru.practicum.shareit.item.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBookingComments;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class ItemMapper {

    private final CommentMapper commentMapper;

    public ItemDto toItemDto(Item item) {
        return ItemDto.builder().id(item.getId()).name(item.getName()).description(item.getDescription())
                .available(item.getAvailable()).itemRequestId(item.getRequest() != null ? item.getRequest() : null)
                .build();
    }

    public Item toItem(ItemDto itemDto, User user) {
        return Item.builder().id(itemDto.getId()).name(itemDto.getName()).description(itemDto.getDescription())
                .available(itemDto.getAvailable()).owner(user).request(itemDto.getItemRequestId()).build();
    }

    public ItemDtoBookingComments toItemDtoBookingComments(Item item, List<Comment> commentList) {
        return ItemDtoBookingComments.builder().id(item.getId()).name(item.getName()).description(item.getDescription())
                .available(item.getAvailable()).lastBooking(null).nextBooking(null)
                .request(item.getRequest() != null ? item.getRequest() : null)
                .comments(commentList.stream().map(commentMapper::toDto).collect(Collectors.toList())).build();
    }
}