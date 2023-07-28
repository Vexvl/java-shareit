package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBookingComments;
import ru.practicum.shareit.item.exception.AccessDeniedException;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {
    @Autowired
    ObjectMapper mapper;
    @MockBean
    private ItemService itemService;
    @Autowired
    private MockMvc mvc;
    private ItemDto itemDto;
    private ItemDtoBookingComments itemDtoBookingComments;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        mapper.registerModule(new JavaTimeModule());

        itemDto = new ItemDto(
                1L,
                "Дрель",
                "Простая дрель",
                true,
                1L
        );

        commentDto = new CommentDto(
                1L,
                "comment",
                "authorName",
                LocalDateTime.of(2023, 10, 10, 10, 10, 10)
        );

        itemDtoBookingComments = new ItemDtoBookingComments(
                1L,
                "Дрель",
                "Простая дрель",
                true,
                1L,
                null,
                null,
                List.of(commentDto)
        );
    }

    @Test
    @SneakyThrows
    void addNewItemTest() {
        when(itemService.addItem(anyLong(), any(ItemDto.class))).thenReturn(itemDto);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId()), Long.class));
    }

    @Test
    @SneakyThrows
    void getAllUserItemTest() {
        when(itemService.getItemsByOwner(anyLong(), anyInt(), anyInt())).thenReturn(List.of(itemDtoBookingComments));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemDtoBookingComments.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDtoBookingComments.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDtoBookingComments.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDtoBookingComments.getAvailable())))
                .andExpect(jsonPath("$[0].request", is(itemDtoBookingComments.getRequest()), Long.class))
                .andExpect(jsonPath("$[0].comments", hasSize(1)));
    }

    @Test
    @SneakyThrows
    void getItemByIdTest() {
        when(itemService.getItem(anyLong(), anyLong())).thenReturn(itemDtoBookingComments);

        mvc.perform(get("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDtoBookingComments.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoBookingComments.getName())))
                .andExpect(jsonPath("$.description", is(itemDtoBookingComments.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDtoBookingComments.getAvailable())))
                .andExpect(jsonPath("$.request", is(itemDtoBookingComments.getRequest()), Long.class))
                .andExpect(jsonPath("$.comments", hasSize(1)));
    }

    @Test
    void editItemTest() throws Exception {
        when(itemService.editItem(anyLong(), anyLong(), any(ItemDto.class))).thenReturn(itemDto);

        mvc.perform(patch("/items/{itemId}", 1L)
                        .content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId()), Long.class));
    }

    @Test
    void editItemWhenUserDontHavePermissionTest() throws Exception {
        when(itemService.editItem(anyLong(), anyLong(), any(ItemDto.class)))
                .thenThrow(new AccessDeniedException("User with id %d does not have permission for do this"));

        mvc.perform(patch("/items/{itemId}", 3L)
                        .content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchItemTest() throws Exception {
        when(itemService.searchItem(anyLong(), anyString(), anyInt(), anyInt())).thenReturn(List.of(itemDto));

        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1)
                        .param("text", "findParam"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$[0].requestId", is(itemDto.getRequestId()), Long.class));
    }
}