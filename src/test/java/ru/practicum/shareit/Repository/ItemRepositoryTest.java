package ru.practicum.shareit.Repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ItemRepositoryTest {

    Pageable pageable = PageRequest.of(0 / 10, 10);
    @Autowired
    private TestEntityManager em;
    @Autowired
    private ItemRepository itemRepository;

    @Test
    void searchTest() {
        User user = new User(
                null,
                "John",
                "first@user.com"
        );
        em.persist(user);
        em.flush();
        Item item = new Item(
                null,
                "Дрель",
                "Простая дрель",
                true,
                user,
                null
        );
        em.persist(item);
        em.flush();
        Iterable<Item> items = itemRepository.searchByText("простая", pageable);
        assertThat(items).hasSize(1).contains(item);
        em.clear();
    }
}