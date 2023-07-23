package ru.practicum.shareit.Integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RequestTest {

    private final EntityManager em;
    private final ItemRequestService itemRequestService;
    private final User user = new User(
            null,
            "John",
            "first@user.com"
    );
    private final ItemRequestDto itemRequestDto = new ItemRequestDto(
            null,
            "description",
            null,
            null,
            null
    );
    private final ItemRequest itemRequest = new ItemRequest(
            null,
            "description",
            user,
            LocalDateTime.now()
    );
    private final LocalDateTime created = LocalDateTime.now();

    @Test
    void createItemRequestTest() {
        User testUser = new User(
                null,
                "John",
                "first@user.com"
        );
        em.persist(testUser);
        em.flush();
        itemRequestService.addItemRequest(itemRequestDto, testUser.getId());
        TypedQuery<ItemRequest> query = em.createQuery("Select i from ItemRequest i", ItemRequest.class);
        List<ItemRequest> getItemRequest = query.getResultList();
        assertEquals(1, getItemRequest.size());
        assertEquals(itemRequest.getDescription(), getItemRequest.get(0).getDescription());
        assertEquals(testUser, getItemRequest.get(0).getRequester());
        em.clear();
    }

    @Test
    void getByRequestIdTest() {
        User testUser = new User(
                null,
                "John",
                "first@user.com"
        );
        em.persist(testUser);
        em.flush();
        ItemRequest testItemRequest = new ItemRequest(
                null,
                "description",
                testUser,
                created
        );
        em.persist(testItemRequest);
        em.flush();
        ItemRequestDto testItemRequestDto = new ItemRequestDto(
                testItemRequest.getId(),
                "description",
                testUser.getId(),
                created,
                List.of()
        );
        ItemRequestDto getItemRequestDto = itemRequestService.getItemRequestById(testUser.getId(), testItemRequest.getId());
        assertEquals(getItemRequestDto, testItemRequestDto);
        em.clear();
    }

}