package ru.practicum.shareit.user.repository.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.exception.AbsenceException;
import ru.practicum.shareit.user.exception.EmailExistence;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class UserRepositoryImpl implements UserRepository {

    private final Map<Long, User> usersMap = new HashMap<>();
    private final Set<String> emailsSet = new HashSet<>();
    private long ownerId;

    @Override
    public User addUser(User user) {
        if (emailsSet.contains(user.getEmail())) {
            throw new EmailExistence("Email уже зарегестрирован");
        }
        ownerId++;
        user.setId(ownerId);
        emailsSet.add(user.getEmail());
        usersMap.put(ownerId, user);
        return user;
    }

    @Override
    public User editUser(Long ownerId, User user) {
        User existingUser = usersMap.get(ownerId);
        if (user != null) {
            if (user.getEmail() != null) {
                String newEmail = user.getEmail();
                if (emailsSet.contains(newEmail) && !existingUser.getEmail().equals(user.getEmail())) {
                    throw new EmailExistence("Email уже используется");
                }
                emailsSet.remove(existingUser.getEmail());
                existingUser.setEmail(newEmail);
                emailsSet.add(user.getEmail());
            }
            if (user.getName() != null) {
                existingUser.setName(user.getName());
            }
        } else {
            throw new IllegalArgumentException("User null");
        }
        return usersMap.get(ownerId);
    }

    @Override
    public Map<Long, User> getAllUsers() {
        return usersMap;
    }

    @Override
    public User getUser(Long ownerId) {
        if (usersMap.containsKey(ownerId)) {
            return usersMap.get(ownerId);
        } else throw new AbsenceException("Такого user нет");
    }

    @Override
    public void deleteUser(Long ownerId) {
        if (usersMap.containsKey(ownerId)) {
            emailsSet.remove(usersMap.get(ownerId).getEmail());
            usersMap.remove(ownerId);
        } else throw new AbsenceException("Такого user нет");
    }

}