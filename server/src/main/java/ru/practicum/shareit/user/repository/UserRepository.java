package src.main.java.ru.practicum.shareit.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import src.main.java.ru.practicum.shareit.user.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}