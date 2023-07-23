package ru.practicum.shareit.request.dto;

import lombok.*;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ItemRequestDto {
    private Long id;
    @Size(max = 500)
    @NotBlank
    private String description;
    private Long requester;
    private LocalDateTime created;
    private List<ItemDto> items;
}