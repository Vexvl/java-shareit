package ru.practicum.shareit.ItemRequest.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.exception.AbsenceException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemRequestTest {

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

    @Test
    void getAllNotOwner_WhenOwnerExistsAndRequestsFound_ThenRequestsSortedByCreatedDesc() {
        int from = 0;
        int size = 10;
        Long ownerId = 1L;

        User requester1 = new User(null, "Requester 1", "requester1@test.com");
        User requester2 = new User(null, "Requester 2", "requester2@test.com");
        User owner = new User(ownerId, "Owner", "owner@test.com");

        ItemRequest itemRequest1 = new ItemRequest(null, "Description 1", requester1, LocalDateTime.now());
        ItemRequest itemRequest2 = new ItemRequest(null, "Description 2", requester2, LocalDateTime.now());
        ItemRequest itemRequest3 = new ItemRequest(null, "Description 3", requester1, LocalDateTime.now().minusHours(1));

        em.persist(requester1);
        em.persist(requester2);
        em.persist(itemRequest1);
        em.persist(itemRequest2);
        em.persist(itemRequest3);
        em.flush();

        List<ItemRequestDto> itemRequests = itemRequestService.getAllNotOwner(ownerId, from, size);

        assertThat(itemRequests).hasSize(1);
        assertThat(itemRequests.get(0).getDescription()).isEqualTo(itemRequest2.getDescription());
    }

    @Test
    void getItemRequestById_WhenOwnerExistsAndRequestNotFound_ThenExceptionThrown() {
        Long ownerId = 1L;
        Long requestId = 1L;

        assertThrows(AbsenceException.class, () -> itemRequestService.getItemRequestById(ownerId, requestId));
    }

    @Test
    void getItemRequestById_WhenOwnerNotFound_ThenExceptionThrown() {
        Long ownerId = 1L;
        Long requestId = 1L;

        assertThrows(AbsenceException.class, () -> itemRequestService.getItemRequestById(ownerId, requestId));
    }
}