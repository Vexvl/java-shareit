package src.main.java.ru.practicum.shareit.user.mapper;

import org.springframework.stereotype.Component;
import src.main.java.ru.practicum.shareit.user.dto.UserDto;
import src.main.java.ru.practicum.shareit.user.model.User;

@Component
public class UserMapper {

    public UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public User toUser(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }
}