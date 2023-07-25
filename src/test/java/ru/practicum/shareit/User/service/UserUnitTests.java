package ru.practicum.shareit.User.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.item.exception.AbsenceException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.EmailDuplicateException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.impl.UserServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserUnitTests {

    private static User mockUser;
    private static UserDto mockUserDto;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserMapper userMapper;

    @BeforeAll
    static void setup() {
        mockUser = mock(User.class);
        mockUserDto = mock(UserDto.class);
    }

    @Test
    void add_whenCreatedWithDuplicateEmail_thenDuplicateEntityExceptionThrown() {
        when(userMapper.toUser(mockUserDto)).thenReturn(mockUser);
        when(userRepository.save(mockUser)).thenThrow(DataIntegrityViolationException.class);
        assertThrows(EmailDuplicateException.class, () -> userService.addUser(mockUserDto));
    }

    @Test
    void update_whenUserNotFound_thenNotExistsExceptionThrown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
        assertThrows(AbsenceException.class, () -> userService.editUser(anyLong(), mockUserDto));
    }

    @Test
    void getById_whenUserNotFound_thenNotExistsExceptionThrown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
        assertThrows(AbsenceException.class, () -> userService.getUser(anyLong()));
    }
}