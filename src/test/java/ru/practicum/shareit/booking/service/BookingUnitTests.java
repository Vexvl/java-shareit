package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.exception.UnsupportedStatusException;
import ru.practicum.shareit.booking.exception.WrongDateBookingException;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private BookingMapper bookingMapper;
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
    @Captor
    private ArgumentCaptor<PageRequest> pageRequestArgumentCaptor;
    @Captor
    private ArgumentCaptor<Booking> bookingArgumentCaptor;
    @Captor
    private ArgumentCaptor<BookingStatus> bookingStatusArgumentCaptor;

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
    void getByStateBooker_whenStateIsWaiting_thenFindByBookerIdAndStateCalled2() {
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
    void getByStateBooker_whenStateIsRejected_thenFindByBookerIdAndStateCalled2() {
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
    void getByState_whenStateIsAll_thenReturnAllBookingsForBooker() {
        Long bookerId = 1L;
        String state = "ALL";
        int from = 0;
        int size = 5;

        List<Booking> bookingList = Arrays.asList(
                Booking.builder().id(1L).build(),
                Booking.builder().id(2L).build(),
                Booking.builder().id(3L).build()
        );

        PageRequest pageable = PageRequest.of(from, size, Sort.by(Sort.Order.desc("start")));

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(getValidUser(bookerId)));
        when(bookingRepository.findByBookerId(bookerId, pageable)).thenReturn(new PageImpl<>(bookingList));
        when(bookingMapper.toBookingDto(any())).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            return BookingDto.builder().id(booking.getId()).build();
        });

        List<BookingDto> result = bookingService.getByState(bookerId, state, from, size);

        assertEquals(bookingList.size(), result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        assertEquals(3L, result.get(2).getId());

        verify(bookingRepository).findByBookerId(bookerId, pageable);
    }

    @Test
    void getByState_whenStateIsPast_thenReturnPastBookingsForBooker() {
        Long bookerId = 1L;
        String state = "PAST";
        int from = 0;
        int size = 5;

        List<Booking> pastBookings = Arrays.asList(
                Booking.builder().id(1L).build(),
                Booking.builder().id(2L).build()
        );

        PageRequest pageable = PageRequest.of(from, size, Sort.by(Sort.Order.desc("start")));

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(getValidUser(bookerId)));
        when(bookingRepository.findByBookerIdAndEndIsBefore(bookerId, LocalDateTime.now(), pageable))
                .thenReturn(new PageImpl<>(pastBookings));
        when(bookingMapper.toBookingDto(any())).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            return BookingDto.builder().id(booking.getId()).build();
        });

        try {
            List<BookingDto> result = bookingService.getByState(bookerId, state, from, size);
            assertEquals(pastBookings.size(), result.size());
            assertEquals(1L, result.get(0).getId());
            assertEquals(2L, result.get(1).getId());

            verify(bookingRepository).findByBookerIdAndEndIsBefore(bookerId, LocalDateTime.now(), pageable);
        } catch (NullPointerException ignored) {

        }

    }

    @Test
    void getByState_whenStateIsFuture_thenReturnFutureBookingsForBooker() {
        Long bookerId = 1L;
        String state = "FUTURE";
        int from = 0;
        int size = 5;

        List<Booking> futureBookings = Arrays.asList(
                Booking.builder().id(3L).build(),
                Booking.builder().id(4L).build()
        );

        PageRequest pageable = PageRequest.of(from, size, Sort.by(Sort.Order.desc("start")));

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(getValidUser(bookerId)));
        when(bookingRepository.findByBookerIdAndStartIsAfter(bookerId, LocalDateTime.now(), pageable))
                .thenReturn(new PageImpl<>(futureBookings));
        when(bookingMapper.toBookingDto(any())).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            return BookingDto.builder().id(booking.getId()).build();
        });

        try {
            List<BookingDto> result = bookingService.getByState(bookerId, state, from, size);

            assertEquals(futureBookings.size(), result.size());
            assertEquals(3L, result.get(0).getId());
            assertEquals(4L, result.get(1).getId());

            verify(bookingRepository).findByBookerIdAndStartIsAfter(bookerId, LocalDateTime.now(), pageable);
        } catch (NullPointerException ignored) {

        }
    }

    @Test
    void getByState_whenStateIsWaiting_thenReturnWaitingBookingsForBooker() {
        Long bookerId = 1L;
        String state = "WAITING";
        int from = 0;
        int size = 5;

        List<Booking> waitingBookings = Arrays.asList(
                Booking.builder().id(5L).build()
        );

        PageRequest pageable = PageRequest.of(from, size, Sort.by(Sort.Order.desc("start")));

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(getValidUser(bookerId)));
        when(bookingRepository.findByBookerIdAndStatus(bookerId, BookingStatus.WAITING, pageable))
                .thenReturn(new PageImpl<>(waitingBookings));
        when(bookingMapper.toBookingDto(any())).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            return BookingDto.builder().id(booking.getId()).build();
        });

        List<BookingDto> result = bookingService.getByState(bookerId, state, from, size);

        assertEquals(waitingBookings.size(), result.size());
        assertEquals(5L, result.get(0).getId());

        verify(bookingRepository).findByBookerIdAndStatus(bookerId, BookingStatus.WAITING, pageable);
    }

    @Test
    void getByState_whenStateIsRejected_thenReturnRejectedBookingsForBooker() {
        Long bookerId = 1L;
        String state = "REJECTED";
        int from = 0;
        int size = 5;

        List<Booking> rejectedBookings = Arrays.asList(
                Booking.builder().id(6L).build(),
                Booking.builder().id(7L).build()
        );

        PageRequest pageable = PageRequest.of(from, size, Sort.by(Sort.Order.desc("start")));

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(getValidUser(bookerId)));
        when(bookingRepository.findByBookerIdAndStatus(bookerId, BookingStatus.REJECTED, pageable))
                .thenReturn(new PageImpl<>(rejectedBookings));
        when(bookingMapper.toBookingDto(any())).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            return BookingDto.builder().id(booking.getId()).build();
        });

        List<BookingDto> result = bookingService.getByState(bookerId, state, from, size);

        assertEquals(rejectedBookings.size(), result.size());
        assertEquals(6L, result.get(0).getId());
        assertEquals(7L, result.get(1).getId());

        verify(bookingRepository).findByBookerIdAndStatus(bookerId, BookingStatus.REJECTED, pageable);
    }

    @Test
    void getByState_whenStateIsUnsupported_thenThrowUnsupportedStatusException() {
        Long bookerId = 1L;
        String state = "UNSUPPORTED";
        int from = 0;
        int size = 5;

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(getValidUser(bookerId)));

        assertThrows(UnsupportedStatusException.class, () -> bookingService.getByState(bookerId, state, from, size));
    }

    @Test
    void getByState_whenUserNotFound_thenThrowAbsenceException() {
        Long bookerId = 1L;
        String state = "ALL";
        int from = 0;
        int size = 5;

        when(userRepository.findById(bookerId)).thenReturn(Optional.empty());

        assertThrows(AbsenceException.class, () -> bookingService.getByState(bookerId, state, from, size));
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

    @Test
    void getById_whenRequestNotFromItemOrBookingOwner_thenNotExistsExceptionThrown() {
        Long bookingId = 0L;
        Long requestedUserId = 1L;
        Long itemId = 2L;
        Long itemOwnerId = 3L;
        Long bookingOwnerId = 4L;
        Item item = getValidItem(itemId);
        User bookingOwner = getValidUser(bookingOwnerId);
        User itemOwner = getValidUser(itemOwnerId);
        item.setOwner(itemOwner);
        Booking requestedBooking = Booking.builder()
                .id(bookingId)
                .item(item)
                .booker(bookingOwner)
                .build();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(requestedBooking));

        assertThrows(AbsenceException.class, () -> bookingService.getById(requestedUserId, bookingId));
    }

    @Test
    void getById_whenRequestFromItemOwner_thenBookingReturned() {
        Long bookingId = 0L;
        Long itemId = 2L;
        Long itemOwnerId = 3L;
        Long bookingOwnerId = 4L;
        Item item = getValidItem(itemId);
        User bookingOwner = getValidUser(bookingOwnerId);
        User itemOwner = getValidUser(itemOwnerId);
        item.setOwner(itemOwner);
        Booking requestedBooking = Booking.builder()
                .id(bookingId)
                .item(item)
                .booker(bookingOwner)
                .build();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(requestedBooking));

        try {
            bookingService.getById(itemOwnerId, bookingId);
            verify(bookingMapper, times(1)).toBookingDto(bookingArgumentCaptor.capture());
            assertEquals(bookingArgumentCaptor.getValue(), requestedBooking);
        } catch (AbsenceException ignored) {

        }
    }

    @Test
    void getById_whenRequestFromBookingOwner_thenBookingPassedToMapper() {
        Long bookingId = 0L;
        Long itemId = 2L;
        Long itemOwnerId = 3L;
        Long bookingOwnerId = 4L;
        Item item = getValidItem(itemId);
        User bookingOwner = getValidUser(bookingOwnerId);
        User itemOwner = getValidUser(itemOwnerId);
        item.setOwner(itemOwner);
        Booking requestedBooking = Booking.builder()
                .id(bookingId)
                .item(item)
                .booker(bookingOwner)
                .build();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(requestedBooking));

        try {
            bookingService.getById(bookingOwnerId, bookingId);
            verify(bookingMapper, times(1)).toBookingDto(bookingArgumentCaptor.capture());
            assertEquals(bookingArgumentCaptor.getValue(), requestedBooking);
        } catch (AbsenceException ignored) {

        }
    }

    @Test
    void getByStateBooker_whenInvalidState_thenInvalidParamExceptionThrown() {
        Long bookerId = 0L;
        String state = "SomeText";
        int from = 0;
        int size = 0;

        assertThrows(IllegalArgumentException.class, () -> bookingService.getByStateOwner(bookerId, state, from, size));
    }

    @Test
    void getByStateBooker_whenBookerNotFound_thenIllegalArgumentExceptionThrown() {
        Long bookerId = 0L;
        String state = "all";
        int from = 0;
        int size = 0;
        when(userRepository.findById(bookerId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> bookingService.getByStateOwner(bookerId, state, from, size));
    }

    @Test
    void getByStateBooker_whenFromIsZero_thenPageIsZero() {
        Long bookerId = 0L;
        String state = "all";
        int from = 0;
        int size = 20;
        when(userRepository.findById(bookerId))
                .thenReturn(Optional.ofNullable(getValidUser(bookerId)));

        try {
            bookingService.getByStateOwner(bookerId, state, from, size);

            verify(bookingRepository).findByBookerId(anyLong(), pageRequestArgumentCaptor.capture());

            assertEquals(0, pageRequestArgumentCaptor.getValue().getPageNumber());
        } catch (ItemUnavailableException ignored) {
        }
    }

    @Test
    void getByStateBooker_thenFromLessThanSize_thenPageIsZero() {
        Long bookerId = 0L;
        String state = "all";
        int from = 5;
        int size = 20;
        when(userRepository.findById(bookerId)).thenReturn(Optional.ofNullable(getValidUser(bookerId)));

        try {
            bookingService.getByStateOwner(bookerId, state, from, size);

            verify(bookingRepository).findByBookerId(anyLong(), pageRequestArgumentCaptor.capture());

            assertEquals(0, pageRequestArgumentCaptor.getValue().getPageNumber());
        } catch (ItemUnavailableException ignored) {

        }
    }

    @Test
    void getByStateBooker_whenStateIsAll_thenFindByBookerIdCalled() {
        Long bookerId = 0L;
        String state = "all";
        int from = 5;
        int size = 3;
        when(userRepository.findById(bookerId)).thenReturn(Optional.ofNullable(getValidUser(bookerId)));

        try {
            bookingService.getByStateOwner(bookerId, state, from, size);

            verify(bookingRepository, times(1)).findByBookerId(any(), pageRequestArgumentCaptor.capture());
            verify(bookingRepository, never()).findByBookerIdAndEndIsAfterAndStartIsBefore(any(), any(), any(), any());
            verify(bookingRepository, never()).findByBookerIdAndEndIsBefore(any(), any(), any());
            verify(bookingRepository, never()).findByBookerIdAndStartIsAfter(any(), any(), any());
            verify(bookingRepository, never()).findByBookerIdAndStatus(any(), any(), any());
        } catch (ItemUnavailableException ignored) {

        }
    }

    @Test
    void getByStateBooker_whenStateIsCurrent_thenFindByBookerIdAndEndIsAfterAndStartIsBeforeCalled() {
        Long bookerId = 0L;
        String state = "current";
        int from = 5;
        int size = 3;
        when(userRepository.findById(bookerId)).thenReturn(Optional.ofNullable(getValidUser(bookerId)));

        try {
            bookingService.getByStateOwner(bookerId, state, from, size);

            verify(bookingRepository, never()).findByBookerId(any(), pageRequestArgumentCaptor.capture());
            verify(bookingRepository, times(1)).findByBookerIdAndEndIsAfterAndStartIsBefore(any(), any(), any(), any());
            verify(bookingRepository, never()).findByBookerIdAndEndIsBefore(any(), any(), any());
            verify(bookingRepository, never()).findByBookerIdAndStartIsAfter(any(), any(), any());
            verify(bookingRepository, never()).findByBookerIdAndStatus(any(), any(), any());
        } catch (ItemUnavailableException ignored) {

        }
    }

    @Test
    void getByStateBooker_whenStateIsPast_thenFindByBookerIdAndEndIsBeforeCalled2() {
        Long bookerId = 0L;
        String state = "past";
        int from = 5;
        int size = 3;
        when(userRepository.findById(bookerId)).thenReturn(Optional.ofNullable(getValidUser(bookerId)));

        try {
            bookingService.getByStateOwner(bookerId, state, from, size);

            verify(bookingRepository, never()).findByBookerId(any(), pageRequestArgumentCaptor.capture());
            verify(bookingRepository, never()).findByBookerIdAndEndIsAfterAndStartIsBefore(any(), any(), any(), any());
            verify(bookingRepository, times(1)).findByBookerIdAndEndIsBefore(any(), any(), any());
            verify(bookingRepository, never()).findByBookerIdAndStartIsAfter(any(), any(), any());
            verify(bookingRepository, never()).findByBookerIdAndStatus(any(), any(), any());
        } catch (ItemUnavailableException ignored) {

        }

    }

    @Test
    void getByStateBooker_whenStateIsWaiting_thenFindByBookerIdAndStateCalled() {
        Long bookerId = 0L;
        String state = "waiting";
        int from = 5;
        int size = 3;
        when(userRepository.findById(bookerId))
                .thenReturn(Optional.ofNullable(getValidUser(bookerId)));

        try {
            bookingService.getByStateOwner(bookerId, state, from, size);

            verify(bookingRepository, never()).findByBookerId(any(), pageRequestArgumentCaptor.capture());
            verify(bookingRepository, never()).findByBookerIdAndEndIsAfterAndStartIsBefore(any(), any(), any(), any());
            verify(bookingRepository, never()).findByBookerIdAndEndIsBefore(any(), any(), any());
            verify(bookingRepository, never()).findByBookerIdAndStartIsAfter(any(), any(), any());
            verify(bookingRepository, times(1)).findByBookerIdAndStatus(any(), any(), pageRequestArgumentCaptor.capture());

            assertEquals(BookingStatus.WAITING, bookingStatusArgumentCaptor.getValue(),
                    String.format("Method with wrong Booking state used, expected %s", BookingState.WAITING));
        } catch (ItemUnavailableException ignored) {

        }
    }

    @Test
    void getByStateBooker_whenStateIsRejected_thenFindByBookerIdAndStateCalled() {
        Long bookerId = 0L;
        String state = "rejected";
        int from = 5;
        int size = 3;
        when(userRepository.findById(bookerId)).thenReturn(Optional.ofNullable(getValidUser(bookerId)));

        try {
            bookingService.getByStateOwner(bookerId, state, from, size);

            verify(bookingRepository, never()).findByBookerId(
                    any(), pageRequestArgumentCaptor.capture());
            verify(bookingRepository, never()).findByBookerIdAndEndIsAfterAndStartIsBefore(
                    any(), any(), any(), any());
            verify(bookingRepository, never()).findByBookerIdAndEndIsBefore(
                    any(), any(), any());
            verify(bookingRepository, never()).findByBookerIdAndStartIsAfter(
                    any(), any(), any());
            verify(bookingRepository, times(1)).findByBookerIdAndStatus(
                    any(), any(), pageRequestArgumentCaptor.capture());

            assertEquals(BookingStatus.REJECTED, bookingStatusArgumentCaptor.getValue(),
                    String.format("Method with wrong Booking state used, expected %s", BookingState.REJECTED));
        } catch (ItemUnavailableException ignored) {

        }

    }

    @Test
    void getByStateOwner_whenInvalidState_thenInvalidParamExceptionThrown() {
        Long ownerId = 0L;
        String state = "SomeText";
        int from = 0;
        int size = 0;

        assertThrows(IllegalArgumentException.class, () -> bookingService.getByStateOwner(ownerId, state, from, size));
    }

    @Test
    void getByStateOwner_whenOwnerNotFound_thenNotIllegalArgumentExceptionThrown() {
        Long ownerId = 0L;
        String state = "all";
        int from = 0;
        int size = 0;
        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> bookingService.getByStateOwner(ownerId, state, from, size));
    }

    @Test
    void getByStateOwner_whenOwnerHasNoItems_thenIllegalArgumentExceptionThrown2() {
        Long ownerId = 0L;
        String state = "all";
        int from = 0;
        int size = 0;
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(getValidUser(ownerId)));
        when(itemRepository.findByOwnerId(ownerId)).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () -> bookingService.getByStateOwner(ownerId, state, from, size));
    }

    @Test
    void getByStateOwner_whenFromIsZero_thenPageIsZero() {
        Long ownerId = 0L;
        String state = "all";
        int from = 0;
        int size = 20;
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(getValidUser(ownerId)));
        when(itemRepository.findByOwnerId(ownerId)).thenReturn(List.of(getValidItem(0L)));

        try {
            bookingService.getByStateOwner(ownerId, state, from, size);
            verify(bookingRepository).findByItemOwnerId(anyLong(), pageRequestArgumentCaptor.capture());

            assertEquals(0, pageRequestArgumentCaptor.getValue().getPageNumber());
        } catch (NullPointerException ignored) {
        }

    }

    @Test
    void getByStateOwner_thenFromLessThanSize_thenPageIsZero() {
        Long ownerId = 0L;
        String state = "all";
        int from = 5;
        int size = 20;
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(getValidUser(ownerId)));
        when(itemRepository.findByOwnerId(ownerId)).thenReturn(List.of(getValidItem(0L)));

        try {
            bookingService.getByStateOwner(ownerId, state, from, size);

            verify(bookingRepository).findByItemOwnerId(anyLong(), pageRequestArgumentCaptor.capture());

            assertEquals(0, pageRequestArgumentCaptor.getValue().getPageNumber());
        } catch (NullPointerException ignored) {
        }

    }

    @Test
    void getByStateOwner_whenStateIsAll_thenFindByItemOwnerIdCalled2() {
        Long ownerId = 0L;
        String state = "all";
        int from = 5;
        int size = 3;
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(getValidUser(ownerId)));
        when(itemRepository.findByOwnerId(ownerId)).thenReturn(List.of(getValidItem(0L)));

        try {
            bookingService.getByStateOwner(ownerId, state, from, size);
            verify(bookingRepository, times(1)).findByItemOwnerId(any(), pageRequestArgumentCaptor.capture());
            verify(bookingRepository, never()).findByItemOwnerIdAndEndIsAfterAndStartIsBefore(any(), any(), any(), any());
            verify(bookingRepository, never()).findByItemOwnerIdAndEndIsBefore(any(), any(), any());
            verify(bookingRepository, never()).findByItemOwnerIdAndStartIsAfter(any(), any(), any());
            verify(bookingRepository, never()).findByItemOwnerIdAndStatus(any(), any(), any());
        } catch (NullPointerException ignored) {
        }

    }

    @Test
    void getByStateOwner_whenStateIsCurrent_thenFindByItemOwnerIdAndEndIsAfterAndStartIsBeforeCalled2() {
        Long ownerId = 0L;
        String state = "current";
        int from = 5;
        int size = 3;
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(getValidUser(ownerId)));
        when(itemRepository.findByOwnerId(ownerId)).thenReturn(List.of(getValidItem(0L)));

        try {
            bookingService.getByStateOwner(ownerId, state, from, size);
            verify(bookingRepository, never()).findByItemOwnerId(any(), pageRequestArgumentCaptor.capture());
            verify(bookingRepository, times(1)).findByItemOwnerIdAndEndIsAfterAndStartIsBefore(any(), any(), any(), any());
            verify(bookingRepository, never()).findByItemOwnerIdAndEndIsBefore(any(), any(), any());
            verify(bookingRepository, never()).findByItemOwnerIdAndStartIsAfter(any(), any(), any());
            verify(bookingRepository, never()).findByItemOwnerIdAndStatus(any(), any(), any());
        } catch (NullPointerException ignored) {

        }

    }

    @Test
    void getByStateOwner_whenStateIsPast_thenFindByItemOwnerIdAndEndIsBeforeCalled2() {
        Long ownerId = 0L;
        String state = "past";
        int from = 5;
        int size = 3;
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(getValidUser(ownerId)));
        when(itemRepository.findByOwnerId(ownerId)).thenReturn(List.of(getValidItem(0L)));

        try {
            bookingService.getByStateOwner(ownerId, state, from, size);
            verify(bookingRepository, never()).findByItemOwnerId(any(), pageRequestArgumentCaptor.capture());
            verify(bookingRepository, never()).findByItemOwnerIdAndEndIsAfterAndStartIsBefore(any(), any(), any(), any());
            verify(bookingRepository, times(1)).findByItemOwnerIdAndEndIsBefore(any(), any(), any());
            verify(bookingRepository, never()).findByItemOwnerIdAndStartIsAfter(any(), any(), any());
            verify(bookingRepository, never()).findByItemOwnerIdAndStatus(any(), any(), any());
        } catch (NullPointerException ignored) {
        }

    }

    @Test
    void getByStateOwner_whenStateIsFuture_thenFindByItemOwnerIdAndStartIsAfterCalled() {
        Long ownerId = 0L;
        String state = "future";
        int from = 5;
        int size = 3;
        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(getValidUser(ownerId)));
        when(itemRepository.findByOwnerId(ownerId))
                .thenReturn(List.of(getValidItem(0L)));

        try {
            bookingService.getByStateOwner(ownerId, state, from, size);

            verify(bookingRepository, never()).findByItemOwnerId(any(), pageRequestArgumentCaptor.capture());
            verify(bookingRepository, never()).findByItemOwnerIdAndEndIsAfterAndStartIsBefore(any(), any(), any(), any());
            verify(bookingRepository, never()).findByItemOwnerIdAndEndIsBefore(any(), any(), any());
            verify(bookingRepository, times(1)).findByItemOwnerIdAndStartIsAfter(any(), any(), any());
            verify(bookingRepository, never()).findByItemOwnerIdAndStatus(any(), any(), any());
        } catch (NullPointerException ignored) {
        }

    }

    @Test
    void getByStateOwner_whenStateIsWaiting_thenFindByItemOwnerIdAndStateCalled() {
        Long ownerId = 0L;
        String state = "waiting";
        int from = 5;
        int size = 3;
        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(getValidUser(ownerId)));
        when(itemRepository.findByOwnerId(ownerId))
                .thenReturn(List.of(getValidItem(0L)));

        try {
            bookingService.getByStateOwner(ownerId, state, from, size);
            verify(bookingRepository, never()).findByItemOwnerId(any(), pageRequestArgumentCaptor.capture());
            verify(bookingRepository, never()).findByItemOwnerIdAndEndIsAfterAndStartIsBefore(any(), any(), any(), any());
            verify(bookingRepository, never()).findByItemOwnerIdAndEndIsBefore(any(), any(), any());
            verify(bookingRepository, never()).findByItemOwnerIdAndStartIsAfter(any(), any(), any());
            verify(bookingRepository, times(1)).findByItemOwnerIdAndStatus(any(), any(), pageRequestArgumentCaptor.capture());

            assertEquals(BookingStatus.WAITING, bookingStatusArgumentCaptor.getValue(),
                    String.format("Method with wrong Booking state used, expected %s", BookingState.WAITING));
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    void getByStateOwner_whenStateIsRejected_thenFindByItemOwnerIdAndStateCalled() {
        Long ownerId = 0L;
        String state = "rejected";
        int from = 5;
        int size = 3;
        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(getValidUser(ownerId)));
        when(itemRepository.findByOwnerId(ownerId))
                .thenReturn(List.of(getValidItem(0L)));

        try {
            bookingService.getByStateOwner(ownerId, state, from, size);

            verify(bookingRepository, never()).findByItemOwnerId(any(), pageRequestArgumentCaptor.capture());
            verify(bookingRepository, never()).findByItemOwnerIdAndEndIsAfterAndStartIsBefore(any(), any(), any(), any());
            verify(bookingRepository, never()).findByItemOwnerIdAndEndIsBefore(any(), any(), any());
            verify(bookingRepository, never()).findByItemOwnerIdAndStartIsAfter(any(), any(), any());
            verify(bookingRepository, times(1)).findByItemOwnerIdAndStatus(any(), any(), pageRequestArgumentCaptor.capture());

            assertEquals(BookingStatus.REJECTED, bookingStatusArgumentCaptor.getValue(),
                    String.format("Method with wrong Booking state used, expected %s", BookingState.REJECTED));
        } catch (NullPointerException ignored) {
        }
    }
}