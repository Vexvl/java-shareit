package ru.practicum.shareit.Integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserTest {

    private final EntityManager em;
    private final UserService service;
    private final UserDto userDto = new UserDto(
            null,
            "John",
            "first@user.com"
    );

    @Test
    void saveUserTest() {
        service.addUser(userDto);
        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User getUser = query.setParameter("email", userDto.getEmail())
                .getSingleResult();
        assertEquals(userDto.getName(), getUser.getName());
        em.clear();
    }

    @Test
    void getAllUserTest() {
        User testUser = new User(
                null,
                "John",
                "first@user.com"
        );
        em.persist(testUser);
        em.flush();
        UserDto testUserDto = new UserDto(
                testUser.getId(),
                "John",
                "first@user.com"
        );
        List<UserDto> getUsers = service.getAllUsers();
        assertEquals(1, getUsers.size());
        assertEquals(testUserDto, getUsers.get(0));
        em.clear();
    }

    @Test
    void updateUserTest() {
        User testUser = new User(
                null,
                "John",
                "first@user.com"
        );
        em.persist(testUser);
        em.flush();
        UserDto updateUser = new UserDto(
                null,
                "NotJohn",
                "second@user.com"
        );
        service.editUser(testUser.getId(), updateUser);
        TypedQuery<User> query = em.createQuery("Select u from User u where u.id = :id", User.class);
        User getUser = query.setParameter("id", testUser.getId())
                .getSingleResult();
        assertEquals(updateUser.getEmail(), getUser.getEmail());
        assertEquals(updateUser.getName(), getUser.getName());
        em.clear();
    }
}