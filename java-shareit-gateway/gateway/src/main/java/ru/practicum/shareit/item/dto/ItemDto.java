package src.main.java.ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private String name;
    @NotBlank
    private String description;
    @NotNull
    private Boolean available;
    @PositiveOrZero
    private Long requestId;
}