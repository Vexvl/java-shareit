package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByOwnerId(Long ownerId);
    @Query("SELECT it FROM Item AS it WHERE it.available = TRUE AND (lower(it.name) LIKE lower(concat('%', ?1, '%')) " +
            "OR lower(it.description) LIKE lower(concat('%', ?1,'%')))")
    List<Item> searchByText(String text);
}