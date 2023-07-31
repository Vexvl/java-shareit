package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByOwnerId(Long ownerId);

    List<Item> findByOwnerId(Pageable page, Long ownerId);

    List<Item> findAllByRequest(Long requestId);

    @Query("SELECT it FROM Item AS it WHERE it.available = TRUE AND (lower(it.name) LIKE lower(concat('%', ?1, '%')) " +
            "OR lower(it.description) LIKE lower(concat('%', ?1,'%')))")
    List<Item> searchByText(String text, Pageable pageable);

    @Query(value = "SELECT * FROM items WHERE request_id IN ?1", nativeQuery = true)
    List<Item> findAllByRequestIn(List<Long> requestIds);
}