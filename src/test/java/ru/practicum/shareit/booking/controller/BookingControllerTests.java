package ru.practicum.shareit.booking.controller;

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
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.exception.WrongDateBookingException;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.AccessDeniedException;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTests {

    @Autowired
    ObjectMapper mapper;
    @MockBean
    private BookingService bookingService;
    @Autowired
    private MockMvc mvc;
    private BookingDto bookingDto;

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
    }

    @Test
    @SneakyThrows
    void addNewBookingTest() {
        when(bookingService.addBooking(any(BookingDto.class), anyLong()))
                .thenReturn(bookingDto);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start").isNotEmpty())
                .andExpect(jsonPath("$.end").isNotEmpty())
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())))
                .andExpect(jsonPath("$.itemId", is(bookingDto.getItemId()), Long.class))
                .andExpect(jsonPath("$.bookerId", is(bookingDto.getBookerId()), Long.class));
    }

    @Test
    @SneakyThrows
    void addNewBookingWhenAccessDeniedExceptionTest() {
        when(bookingService.addBooking(any(BookingDto.class), anyLong()))
                .thenThrow(new AccessDeniedException("AccessDeniedException"));

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void addNewBookingWhenTimeBookingIncorrectTest() {
        when(bookingService.addBooking(any(BookingDto.class), anyLong()))
                .thenThrow(new WrongDateBookingException("WrongDateBookingException"));

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void editItemTest() {
        when(bookingService.editBookingStatus(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(bookingDto);

        mvc.perform(patch("/bookings/{bookingId}", 1L)
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start").isNotEmpty())
                .andExpect(jsonPath("$.end").isNotEmpty())
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())))
                .andExpect(jsonPath("$.itemId", is(bookingDto.getItemId()), Long.class))
                .andExpect(jsonPath("$.bookerId", is(bookingDto.getBookerId()), Long.class));
    }

    @Test
    @SneakyThrows
    void getBookingInfoByIdTest() {
        when(bookingService.getById(anyLong(), anyLong()))
                .thenReturn(bookingDto);

        mvc.perform(get("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start").isNotEmpty())
                .andExpect(jsonPath("$.end").isNotEmpty())
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())))
                .andExpect(jsonPath("$.itemId", is(bookingDto.getItemId()), Long.class))
                .andExpect(jsonPath("$.bookerId", is(bookingDto.getBookerId()), Long.class));
    }

    @Test
    @SneakyThrows
    void getUserItemsBookingTest() {
        when(bookingService.getByStateOwner(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].start").isNotEmpty())
                .andExpect(jsonPath("$[0].end").isNotEmpty())
                .andExpect(jsonPath("$[0].status", is(bookingDto.getStatus().toString())))
                .andExpect(jsonPath("$[0].itemId", is(bookingDto.getItemId()), Long.class))
                .andExpect(jsonPath("$[0].bookerId", is(bookingDto.getBookerId()), Long.class));
    }
}