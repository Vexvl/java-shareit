package src.main.java.ru.practicum.shareit.request.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import src.main.java.ru.practicum.shareit.item.dto.ItemDto;
import src.main.java.ru.practicum.shareit.request.dto.ItemRequestDto;
import src.main.java.ru.practicum.shareit.request.model.ItemRequest;
import src.main.java.ru.practicum.shareit.user.model.User;

import java.util.List;

@Component
@AllArgsConstructor
public class ItemRequestMapper {

    public ItemRequestDto toItemRequestDto(ItemRequest itemRequest, List<ItemDto> itemsDto) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .requester(itemRequest.getRequester().getId())
                .created(itemRequest.getCreated())
                .items(itemsDto)
                .build();
    }

    public ItemRequest toItemRequest(ItemRequestDto itemRequestDto, User user) {
        return ItemRequest.builder()
                .id(itemRequestDto.getId())
                .description(itemRequestDto.getDescription())
                .requester(user)
                .created(itemRequestDto.getCreated())
                .build();
    }
}