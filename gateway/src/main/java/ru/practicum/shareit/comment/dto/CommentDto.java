package ru.practicum.shareit.comment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CommentDto {
    private Long id;
    @NotBlank
    @Size(max = 512)
    private String text;
    private String authorName;
    private LocalDateTime created;
}
