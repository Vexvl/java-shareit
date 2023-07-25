package ru.practicum.shareit.User.dto;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class UserMapperTests {

    private UserMapper userMapper;

    @Before
    public void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    public void testToUserDto() {
        User user = User.builder()
                .id(1L)
                .name("John")
                .email("john@example.com")
                .build();

        UserDto userDto = userMapper.toUserDto(user);

        assertEquals(1L, userDto.getId().longValue());
        assertEquals("John", userDto.getName());
        assertEquals("john@example.com", userDto.getEmail());
    }

    @Test
    public void testToUser() {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("John")
                .email("john@example.com")
                .build();

        User user = userMapper.toUser(userDto);

        assertEquals(1L, user.getId().longValue());
        assertEquals("John", user.getName());
        assertEquals("john@example.com", user.getEmail());
    }
}