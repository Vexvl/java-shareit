package src.main.java.ru.practicum.shareit.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import src.main.java.ru.practicum.shareit.item.exception.AbsenceException;
import src.main.java.ru.practicum.shareit.user.dto.UserDto;
import src.main.java.ru.practicum.shareit.user.exception.EmailDuplicateException;
import src.main.java.ru.practicum.shareit.user.mapper.UserMapper;
import src.main.java.ru.practicum.shareit.user.model.User;
import src.main.java.ru.practicum.shareit.user.repository.UserRepository;
import src.main.java.ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto addUser(UserDto userDto) {
        try {
            User user = userRepository.save(userMapper.toUser(userDto));
            return userMapper.toUserDto(user);
        } catch (DataIntegrityViolationException e) {
            throw new EmailDuplicateException("Email exists");
        }
    }

    @Override
    @Transactional
    public UserDto getUser(Long ownerId) {
        User user = userRepository.findById(ownerId).orElseThrow(() -> new AbsenceException("User not exists"));
        return userMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto editUser(Long ownerId, UserDto userDto) {
        User user = userRepository.findById(ownerId).orElseThrow(() -> new AbsenceException("User not exists"));
        User newUser = userMapper.toUser(userDto);
        if (newUser.getEmail() != null) {
            Optional<User> userWithSameEmail = userRepository.findByEmail(newUser.getEmail());
            if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(ownerId)) {
                throw new EmailDuplicateException("Email exists");
            } else {
                user.setEmail(newUser.getEmail());
            }
        }
        if (newUser.getName() != null) {
            user.setName(newUser.getName());
        }
        return userMapper.toUserDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long removeId) {
        userRepository.deleteById(removeId);
    }
}