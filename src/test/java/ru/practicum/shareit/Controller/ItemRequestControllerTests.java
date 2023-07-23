package ru.practicum.shareit.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTests {
    @Autowired
    ObjectMapper mapper;
    @MockBean
    private ItemRequestService requestService;
    @Autowired
    private MockMvc mvc;
    private ItemRequestDto itemRequestDto;

    @BeforeEach
    void setUp() {
        mapper.registerModule(new JavaTimeModule());

        ItemDto itemDto = new ItemDto(
                1L,
                "Дрель",
                "Простая дрель",
                true,
                1L
        );

        itemRequestDto = new ItemRequestDto(
                1L,
                "description",
                1L,
                LocalDateTime.of(2025, 10, 10, 10, 10, 10),
                List.of(itemDto)
        );
    }

    @Test
    void addNewItemRequestTest() throws Exception {
        when(requestService.addItemRequest(any(ItemRequestDto.class), anyLong()))
                .thenReturn(itemRequestDto);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.requester_id", is(itemRequestDto.getRequester_id()), Long.class))
                .andExpect(jsonPath("$.created").isNotEmpty())
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Test
    void getAllUserItemTest() throws Exception {
        when(requestService.getOwnerResponse(anyLong()))
                .thenReturn(List.of(itemRequestDto));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$[0].requester_id", is(itemRequestDto.getRequester_id()), Long.class))
                .andExpect(jsonPath("$[0].created").isNotEmpty())
                .andExpect(jsonPath("$[0].items", hasSize(1)));
    }

    @Test
    void getOtherItemRequest() throws Exception {
        when(requestService.getAllNotOwner(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(itemRequestDto));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$[0].requester_id", is(itemRequestDto.getRequester_id()), Long.class))
                .andExpect(jsonPath("$[0].created").isNotEmpty())
                .andExpect(jsonPath("$[0].items", hasSize(1)));
    }

    @Test
    void getItemRequestById() throws Exception {
        when(requestService.getItemRequestById(anyLong(), anyLong()))
                .thenReturn(itemRequestDto);

        mvc.perform(get("/requests/{requestId}", 1L)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.requester_id", is(itemRequestDto.getRequester_id()), Long.class))
                .andExpect(jsonPath("$.created").isNotEmpty())
                .andExpect(jsonPath("$.items", hasSize(1)));
    }
}