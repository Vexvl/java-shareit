package ru.practicum.shareit.User.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    private void addUsers() {
        userRepository.save(User.builder()
                .name("user_1")
                .email("email_1@email.ru")
                .build());
    }

    @Test
    void findByEmail_whenInvoked_thenUserFound() {
        Optional<User> actualUser = userRepository.findByEmail("email_1@email.ru");

        assertTrue(actualUser.isPresent());
    }

    @Test
    void save_whenDuplicateEmail_thenDataIntegrityViolationExceptionThrown() {

        Assertions.assertThrows(DataIntegrityViolationException.class,
                () -> userRepository.save(User.builder()
                        .id(0L)
                        .name("user_2")
                        .email("email_1@email.ru")
                        .build())
        );
    }
}