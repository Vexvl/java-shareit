package src.main.java.ru.practicum.shareit.user.service;

import src.main.java.ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto addUser(UserDto userDto);

    UserDto getUser(Long ownerId);

    List<UserDto> getAllUsers();

    UserDto editUser(Long ownerId, UserDto userDto);

    void deleteUser(Long ownerId);
}