package ru.practicum.shareit.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.exception.AbsenceException;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.EmailDuplicateException;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.ValidationException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = UserController.class)
public class UserControllerTests {
    @Autowired
    ObjectMapper mapper;
    @MockBean
    private UserService userService;
    @Autowired
    private MockMvc mvc;
    private UserDto userDto;
    private UserDto badEmail;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(
                1L,
                "John",
                "user@user.com"
        );

        badEmail = new UserDto(
                1L,
                "John",
                "badEmail"
        );
    }

    @Test
    void addNewUser() throws Exception {
        when(userService.addUser(any(UserDto.class)))
                .thenReturn(userDto);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @Test
    void addNewButEmailDuplicateException() throws Exception {
        when(userService.addUser(any(UserDto.class)))
                .thenThrow(new EmailDuplicateException("Email already exist"));

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void addNewButEmailNotCorrectTest() throws Exception {
        when(userService.addUser(any(UserDto.class)))
                .thenThrow(new ValidationException());

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(badEmail))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllUserTest() throws Exception {
        when(userService.getAllUsers())
                .thenReturn(List.of(userDto));

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(userDto.getName())))
                .andExpect(jsonPath("$[0].email", is(userDto.getEmail())));
    }

    @Test
    void getUserByIdTest() throws Exception {
        when(userService.getUser(anyLong()))
                .thenReturn(userDto);

        mvc.perform(get("/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @Test
    void getUserWhenHisNotExistTest() throws Exception {
        when(userService.getUser(anyLong()))
                .thenThrow(new AbsenceException("User with id %d does not exist"));

        mvc.perform(get("/users/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void editUserTest() throws Exception {
        when(userService.editUser(anyLong(), any(UserDto.class)))
                .thenReturn(userDto);

        mvc.perform(patch("/users/{id}", 1L)
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @Test
    void deleteUserTest() throws Exception {
        doNothing().when(userService).deleteUser(anyLong());

        mvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isOk());
    }
}