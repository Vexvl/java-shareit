package ru.practicum.shareit.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    @Override
    public UserDto addUser(UserDto userDto) {
        User user = userMapper.toUser(userDto);
        return userMapper.toUserDto(userRepository.addUser(user));
    }

    @Override
    public UserDto getUser(Long ownerId) {
        User requestedUser = userRepository.getUser(ownerId);
        return userMapper.toUserDto(requestedUser);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.getAllUsers().values().stream().map(userMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    public UserDto editUser(Long ownerId, UserDto userDto) {
        User user = userMapper.toUser(userDto);
        return userMapper.toUserDto(userRepository.editUser(ownerId, user));
    }

    @Override
    public void deleteUser(Long removeId) {
        userRepository.deleteUser(removeId);
    }
}
