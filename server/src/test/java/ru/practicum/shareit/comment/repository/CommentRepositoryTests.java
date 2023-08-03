package ru.practicum.shareit.comment.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

@DataJpaTest
class CommentRepositoryTests {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    @Test
    void testFindAllByItem_ShouldReturnCommentsForItem() {
        User author = saveRandomUser();
        Item item = saveRandomItem(author);

        Comment comment1 = commentRepository.save(Comment.builder()
                .text("Comment 1")
                .author(author)
                .item(item)
                .created(LocalDateTime.now())
                .build());

        Comment comment2 = commentRepository.save(Comment.builder()
                .text("Comment 2")
                .author(author)
                .item(item)
                .created(LocalDateTime.now())
                .build());

        List<Comment> comments = commentRepository.findAllByItem(item);

        assertThat(comments, containsInAnyOrder(comment1, comment2));
    }

    @Test
    void testFindAllByItemIn_ShouldReturnCommentsForItems() {
        User author = saveRandomUser();
        Item item1 = saveRandomItem(author);
        Item item2 = saveRandomItem(author);

        Comment comment1 = commentRepository.save(Comment.builder()
                .text("Comment 1")
                .author(author)
                .item(item1)
                .created(LocalDateTime.now())
                .build());

        Comment comment2 = commentRepository.save(Comment.builder()
                .text("Comment 2")
                .author(author)
                .item(item2)
                .created(LocalDateTime.now())
                .build());

        List<Item> items = List.of(item1, item2);
        List<Comment> comments = commentRepository.findAllByItemIn(items);

        assertThat(comments, containsInAnyOrder(comment1, comment2));
    }

    private User saveRandomUser() {
        return userRepository.save(User.builder()
                .name("name")
                .email(String.format("%s%s@email.ru", "email", new Random().nextInt(9999)))
                .build());
    }

    private Item saveRandomItem(User owner) {
        return itemRepository.save(Item.builder()
                .name("name")
                .description("description")
                .available(true)
                .owner(owner)
                .build());
    }
}