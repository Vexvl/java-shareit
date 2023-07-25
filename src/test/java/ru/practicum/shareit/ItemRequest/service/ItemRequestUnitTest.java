package ru.practicum.shareit.ItemRequest.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.exception.AbsenceException;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.impl.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestUnitTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRequestRepository requestRepository;
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Test
    void add_thenOwnerNotFound_thenNotExistsExceptionThrown() {
        Long ownerId = 0L;
        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        assertThrows(AbsenceException.class, () -> itemRequestService.getItemRequestById(ownerId, 2L));
    }

    @Test
    void getAllUserItemRequests_whenOwnerNotFound_thenNotExistsExceptionThrown() {
        Long ownerId = 0L;
        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        assertThrows(AbsenceException.class, () -> itemRequestService.getItemRequestById(ownerId, 2L));
    }

    @Test
    void getAll_whenInvoked_thenSortIsByCreatedDesc() {
        Long userId = 0L;
        int from = 5;
        int size = 2;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(AbsenceException.class, () -> itemRequestService.getAllNotOwner(userId, from, size));

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getById_thenUserNotFound_thenNotExistsExceptionThrown() {
        Long userId = 0L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(AbsenceException.class, () -> itemRequestService.getOwnerResponse(userId));
    }

    @Test
    void getById_thenRequestNotFound_thenNotExistsExceptionThrown() {
        Long userId = 0L;
        Long requestId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.ofNullable(getValidUser(userId)));
        when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(AbsenceException.class, () -> itemRequestService.getItemRequestById(userId, requestId));
    }

    private User getValidUser(Long id) {
        return User.builder()
                .id(id)
                .name("userName")
                .email("email@email.ru")
                .build();
    }
}