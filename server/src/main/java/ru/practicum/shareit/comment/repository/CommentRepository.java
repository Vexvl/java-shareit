package src.main.java.ru.practicum.shareit.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import src.main.java.ru.practicum.shareit.comment.model.Comment;
import src.main.java.ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByItem(Item item);

    List<Comment> findAllByItemIn(List<Item> itemList);
}