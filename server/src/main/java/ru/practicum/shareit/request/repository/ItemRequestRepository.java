package src.main.java.ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import src.main.java.ru.practicum.shareit.request.model.ItemRequest;
import src.main.java.ru.practicum.shareit.user.model.User;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findAllByRequester(User requester);

    List<ItemRequest> findAllByRequesterNot(User requester, Pageable pageable);

}