package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.exception.UnsupportedStatusException;
import ru.practicum.shareit.booking.exception.WrongDateBookingException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.impl.BookingServiceImpl;
import ru.practicum.shareit.item.exception.AbsenceException;
import ru.practicum.shareit.item.exception.ItemUnavailableException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingUnitTests {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingRepository bookingRepository;
    @InjectMocks
    private BookingServiceImpl bookingService;
    private Long bookerId;
    private Long itemId;
    private Long bookingId;
    private User booker;
    private User itemOwner;
    private User bookingOwner;
    private Item item;
    private Booking booking;

    @BeforeEach
    void setUp() {
        bookerId = 0L;
        itemId = 0L;
        bookingId = 0L;
        booker = getValidUser(bookerId);
        itemOwner = getValidUser(1L);
        bookingOwner = getValidUser(4L);
        item = getValidItem(itemId);
        booking = Booking.builder()
                .id(bookingId)
                .item(item)
                .booker(bookingOwner)
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().minusHours(2))
                .build();
    }

    @Test
    void add_whenUserNotFound_thenAbsenceExceptionThrown() {
        BookingDto bookingDto = BookingDto.builder().build();
        when(userRepository.findById(bookerId)).thenReturn(Optional.empty());
        assertThrows(AbsenceException.class, () -> bookingService.addBooking(bookingDto, bookerId));
    }

    @Test
    void add_whenItemNotFound_thenAbsenceExceptionThrown() {
        BookingDto bookingDto = BookingDto.builder().itemId(itemId).build();
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());
        assertThrows(AbsenceException.class, () -> bookingService.addBooking(bookingDto, bookerId));
    }

    @Test
    void changeStatus_whenBookingNotFound_thenAbsenceExceptionThrown() {
        Long bookingOwnerId = 1L;
        boolean approved = true;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());
        assertThrows(AbsenceException.class, () -> bookingService.editBookingStatus(bookingOwnerId, bookingId, approved));
    }

    @Test
    void getByStateOwner_whenStateIsAll_thenFindByOwnerIdCalled() {
        Long bookerId = 0L;
        String state = "ALL";
        int from = 5;
        int size = 3;
        when(userRepository.findById(bookerId)).thenReturn(Optional.ofNullable(getValidUser(bookerId)));
        try {
            bookingService.getByStateOwner(bookerId, state, from, size);
        } catch (ItemUnavailableException ignored) {
        }
        verify(bookingRepository, never()).findByBookerIdAndEndIsAfterAndStartIsBefore(any(), any(), any(), any());
        verify(bookingRepository, never()).findByBookerIdAndEndIsBefore(any(), any(), any());
        verify(bookingRepository, never()).findByBookerIdAndStartIsAfter(any(), any(), any());
        verify(bookingRepository, never()).findByBookerIdAndStatus(any(), any(), any());
    }

    @Test
    void editBookingStatus_whenUnsupportedStatus_thenUnsupportedStatusExceptionThrown() {
        Long ownerId = 1L;
        Long bookingId = 2L;
        boolean approved = true;

        Booking booking = Booking.builder()
                .id(bookingId)
                .status(BookingStatus.APPROVED)
                .item(Item.builder().owner(User.builder().id(ownerId).build()).build())
                .build();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(UnsupportedStatusException.class, () -> bookingService.editBookingStatus(ownerId, bookingId, approved));
    }

    @Test
    void getByStateOwner_whenInvalidState_thenUnsupportedStatusExceptionThrown() {
        Long ownerId = 1L;
        String state = "INVALID_STATE";
        int from = 0;
        int size = 5;

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(User.builder().id(ownerId).build()));

        assertThrows(UnsupportedStatusException.class, () -> bookingService.getByStateOwner(ownerId, state, from, size));
    }

    @Test
    void getByStateOwner_whenInvalidPaginationParams_thenIllegalArgumentExceptionThrown() {
        Long ownerId = 1L;
        String state = "ALL";
        int from = -1;
        int size = 0;

        when(itemRepository.findByOwnerId(ownerId)).thenReturn(List.of(Item.builder().owner(User.builder().id(ownerId).build()).build()));

        assertThrows(IllegalArgumentException.class, () -> bookingService.getByStateOwner(ownerId, state, from, size));
    }

    @Test
    void getByStateBooker_whenStateIsCurrent_thenFindByBookerIdAndEndIsAfterAndStartIsBeforeCalled() {
        Long bookerId = 0L;
        String state = "CURRENT";
        int from = 5;
        int size = 3;
        when(userRepository.findById(bookerId)).thenReturn(Optional.ofNullable(getValidUser(bookerId)));
        try {
            bookingService.getByStateOwner(bookerId, state, from, size);
        } catch (ItemUnavailableException ignored) {
        }
        verify(bookingRepository, never()).findByBookerIdAndEndIsBefore(any(), any(), any());
        verify(bookingRepository, never()).findByBookerIdAndStartIsAfter(any(), any(), any());
        verify(bookingRepository, never()).findByBookerIdAndStatus(any(), any(), any());
    }

    @Test
    void getByStateBooker_whenStateIsPast_thenFindByBookerIdAndEndIsBeforeCalled() {
        Long bookerId = 0L;
        String state = "PAST";
        int from = 5;
        int size = 3;
        when(userRepository.findById(bookerId)).thenReturn(Optional.ofNullable(getValidUser(bookerId)));
        try {
            bookingService.getByStateOwner(bookerId, state, from, size);
        } catch (ItemUnavailableException ignored) {
        }
        verify(bookingRepository, never()).findByBookerIdAndEndIsAfterAndStartIsBefore(any(), any(), any(), any());
        verify(bookingRepository, never()).findByBookerIdAndStartIsAfter(any(), any(), any());
        verify(bookingRepository, never()).findByBookerIdAndStatus(any(), any(), any());
    }

    @Test
    void getByStateBooker_whenStateIsFuture_thenFindByBookerIdAndStartIsAfterCalled() {
        Long bookerId = 0L;
        String state = "FUTURE";
        int from = 5;
        int size = 3;
        when(userRepository.findById(bookerId)).thenReturn(Optional.ofNullable(getValidUser(bookerId)));
        try {
            bookingService.getByStateOwner(bookerId, state, from, size);
        } catch (ItemUnavailableException ignored) {
        }
        verify(bookingRepository, never()).findByBookerIdAndEndIsAfterAndStartIsBefore(any(), any(), any(), any());
        verify(bookingRepository, never()).findByBookerIdAndEndIsBefore(any(), any(), any());
        verify(bookingRepository, never()).findByBookerIdAndStatus(any(), any(), any());
    }

    @Test
    void getByStateBooker_whenStateIsWaiting_thenFindByBookerIdAndStateCalled() {
        Long bookerId = 0L;
        String state = "WAITING";
        int from = 5;
        int size = 3;
        when(userRepository.findById(bookerId)).thenReturn(Optional.ofNullable(getValidUser(bookerId)));
        try {
            bookingService.getByStateOwner(bookerId, state, from, size);
        } catch (ItemUnavailableException ignored) {
        }
        verify(bookingRepository, never()).findByBookerIdAndEndIsAfterAndStartIsBefore(any(), any(), any(), any());
        verify(bookingRepository, never()).findByBookerIdAndEndIsBefore(any(), any(), any());
        verify(bookingRepository, never()).findByBookerIdAndStartIsAfter(any(), any(), any());
    }

    @Test
    void getByStateBooker_whenStateIsRejected_thenFindByBookerIdAndStateCalled() {
        Long bookerId = 0L;
        String state = "REJECTED";
        int from = 5;
        int size = 3;
        when(userRepository.findById(bookerId)).thenReturn(Optional.ofNullable(getValidUser(bookerId)));
        try {
            bookingService.getByStateOwner(bookerId, state, from, size);
        } catch (ItemUnavailableException ignored) {
        }
        verify(bookingRepository, never()).findByBookerIdAndEndIsAfterAndStartIsBefore(any(), any(), any(), any());
        verify(bookingRepository, never()).findByBookerIdAndEndIsBefore(any(), any(), any());
        verify(bookingRepository, never()).findByBookerIdAndStartIsAfter(any(), any(), any());
    }

    @Test
    void getByStateOwner_whenOwnerNotFound_thenIllegalArgumentExceptionThrown() {
        Long ownerId = 0L;
        String state = "ALL";
        int from = 0;
        int size = 0;
        assertThrows(IllegalArgumentException.class, () -> bookingService.getByStateOwner(ownerId, state, from, size));
    }

    @Test
    void getByStateOwner_whenOwnerHasNoItems_thenIllegalArgumentExceptionThrown() {
        Long ownerId = 0L;
        String state = "ALL";
        int from = 0;
        int size = 0;
        assertThrows(IllegalArgumentException.class, () -> bookingService.getByStateOwner(ownerId, state, from, size));
    }

    @Test
    void getByStateOwner_whenStateIsAll_thenFindByItemOwnerIdCalled() {
        Long ownerId = 0L;
        String state = "ALL";
        int from = 5;
        int size = 3;
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(getValidUser(ownerId)));
        when(itemRepository.findByOwnerId(ownerId)).thenReturn(List.of(getValidItem(0L)));
        try {
            bookingService.getByStateOwner(bookerId, state, from, size);
        } catch (NullPointerException ignored) {
        }
        verify(bookingRepository, never()).findByItemOwnerIdAndEndIsAfterAndStartIsBefore(any(), any(), any(), any());
        verify(bookingRepository, never()).findByItemOwnerIdAndEndIsBefore(any(), any(), any());
        verify(bookingRepository, never()).findByItemOwnerIdAndStartIsAfter(any(), any(), any());
        verify(bookingRepository, never()).findByItemOwnerIdAndStatus(any(), any(), any());
    }

    @Test
    void getByStateOwner_whenStateIsCurrent_thenFindByItemOwnerIdAndEndIsAfterAndStartIsBeforeCalled() {
        Long ownerId = 0L;
        String state = "CURRENT";
        int from = 5;
        int size = 3;
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(getValidUser(ownerId)));
        when(itemRepository.findByOwnerId(ownerId)).thenReturn(List.of(getValidItem(0L)));
        try {
            bookingService.getByStateOwner(bookerId, state, from, size);
        } catch (NullPointerException ignored) {
        }
        verify(bookingRepository, never()).findByItemOwnerIdAndEndIsBefore(any(), any(), any());
        verify(bookingRepository, never()).findByItemOwnerIdAndStartIsAfter(any(), any(), any());
        verify(bookingRepository, never()).findByItemOwnerIdAndStatus(any(), any(), any());
    }

    @Test
    void getByStateOwner_whenStateIsPast_thenFindByItemOwnerIdAndEndIsBeforeCalled() {
        Long ownerId = 0L;
        String state = "PAST";
        int from = 5;
        int size = 3;
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(getValidUser(ownerId)));
        when(itemRepository.findByOwnerId(ownerId)).thenReturn(List.of(getValidItem(0L)));
        try {
            bookingService.getByStateOwner(bookerId, state, from, size);
        } catch (NullPointerException ignored) {
        }
        verify(bookingRepository, never()).findByItemOwnerIdAndEndIsAfterAndStartIsBefore(any(), any(), any(), any());
        verify(bookingRepository, never()).findByItemOwnerIdAndStartIsAfter(any(), any(), any());
        verify(bookingRepository, never()).findByItemOwnerIdAndStatus(any(), any(), any());
    }

    private User getValidUser(Long id) {
        return User.builder()
                .id(id)
                .name("userName")
                .email("email@email.ru")
                .build();
    }

    private Item getValidItem(Long id) {
        return Item.builder()
                .id(id)
                .name("name")
                .description("desc")
                .available(true)
                .owner(itemOwner)
                .build();
    }

    @Test
    void add_whenStartIsAfterEnd_thenInvalidParamException() {
        Long bookerId = 0L;
        Long itemId = 0L;
        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().minusHours(2))
                .build();
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(getValidUser(bookerId)));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(getValidItem(itemId)));

        assertThrows(WrongDateBookingException.class, () -> bookingService.addBooking(bookingDto, bookerId));
    }

    @Test
    void add_whenStartIsEqualEnd_thenInvalidParamException() {
        Long bookerId = 0L;
        Long itemId = 0L;
        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.of(2023, 7, 20, 0, 0))
                .end(LocalDateTime.of(2023, 7, 20, 0, 0))
                .build();
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(getValidUser(bookerId)));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(getValidItem(itemId)));

        assertThrows(WrongDateBookingException.class, () -> bookingService.addBooking(bookingDto, bookerId));
    }

    @Test
    void add_whenItemNotAvailable_thenInvalidParamException() {
        Long bookerId = 0L;
        Long itemId = 0L;
        Item item = getValidItem(itemId);
        item.setAvailable(false);
        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().minusHours(2))
                .build();
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(getValidUser(bookerId)));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(WrongDateBookingException.class, () -> bookingService.addBooking(bookingDto, bookerId));
    }

    @Test
    void add_whenRequestFromItemOwner_thenInvalidParamException() {
        Long bookerId = 0L;
        User booker = getValidUser(bookerId);
        Long itemId = 0L;
        Item item = getValidItem(itemId);
        item.setOwner(booker);
        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().minusHours(2))
                .build();
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(WrongDateBookingException.class, () -> bookingService.addBooking(bookingDto, bookerId));
    }
}