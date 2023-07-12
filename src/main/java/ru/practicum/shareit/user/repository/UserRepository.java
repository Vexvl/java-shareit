package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.Map;

public interface UserRepository {

    User addUser(User user);

    User editUser(Long ownerId, User user);

    Map<Long, User> getAllUsers();

    User getUser(Long ownerId);

    void deleteUser(Long ownerId);

}