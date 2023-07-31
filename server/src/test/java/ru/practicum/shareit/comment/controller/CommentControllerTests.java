package ru.practicum.shareit.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.service.CommentService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CommentController.class)
public class CommentControllerTests {
    @Autowired
    ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private CommentService commentService;
    private BookingDto bookingDto;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {

        ItemDto itemDto = new ItemDto(
                1L,
                "Дрель",
                "Простая дрель",
                true,
                1L
        );

        UserDto userDto = new UserDto(
                1L,
                "John",
                "user@user.com"
        );

        bookingDto = new BookingDto(
                1L,
                LocalDateTime.of(2023, 10, 10, 10, 10, 10),
                LocalDateTime.of(2030, 10, 10, 10, 10, 10),
                BookingStatus.APPROVED,
                2L,
                3L,
                userDto,
                itemDto
        );

        commentDto = new CommentDto(
                1L,
                "comment",
                "me",
                LocalDateTime.of(2030, 10, 10, 10, 10, 10));
    }

    @Test
    @SneakyThrows
    void addComment() {
        Long itemId = 1L;
        when(commentService.addComment(anyLong(), anyLong(), any(CommentDto.class))).thenReturn(commentDto);
        String responseJson = mvc.perform(post("/items/{itemId}/comment", itemId)
                        .content(mapper.writeValueAsString(commentDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CommentDto responseDto = mapper.readValue(responseJson, CommentDto.class);
        assertThat(responseDto.getId(), is(commentDto.getId()));
        assertThat(responseDto.getAuthorName(), is(commentDto.getAuthorName()));
        assertThat(responseDto.getText(), is(commentDto.getText()));
        assertThat(responseDto.getCreated(), is(LocalDateTime.parse("2030-10-10T10:10:10")));
    }
}