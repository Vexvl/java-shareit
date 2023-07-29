package src.main.java.ru.practicum.shareit.booking.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import src.main.java.ru.practicum.shareit.booking.dto.BookingDto;
import src.main.java.ru.practicum.shareit.booking.exception.UnsupportedStatusException;
import src.main.java.ru.practicum.shareit.booking.exception.WrongDateBookingException;
import src.main.java.ru.practicum.shareit.booking.mapper.BookingMapper;
import src.main.java.ru.practicum.shareit.booking.model.Booking;
import src.main.java.ru.practicum.shareit.booking.model.BookingState;
import src.main.java.ru.practicum.shareit.booking.model.BookingStatus;
import src.main.java.ru.practicum.shareit.booking.repository.BookingRepository;
import src.main.java.ru.practicum.shareit.booking.service.BookingService;
import src.main.java.ru.practicum.shareit.item.exception.AbsenceException;
import src.main.java.ru.practicum.shareit.item.exception.AccessDeniedException;
import src.main.java.ru.practicum.shareit.item.exception.ItemUnavailableException;
import src.main.java.ru.practicum.shareit.item.exception.OwnerBookingException;
import src.main.java.ru.practicum.shareit.item.model.Item;
import src.main.java.ru.practicum.shareit.item.repository.ItemRepository;
import src.main.java.ru.practicum.shareit.user.model.User;
import src.main.java.ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final Sort sortByStartDesc = Sort.by(Sort.Direction.DESC, "start");

    @Override
    @Transactional
    public BookingDto addBooking(BookingDto bookingDto, Long ownerId) {
        User user = userRepository.findById(ownerId).orElseThrow(() -> new AbsenceException("User not exists"));
        Item item = itemRepository.findById(bookingDto.getItemId()).orElseThrow(() ->
                new AbsenceException("Item not exists"));
        if (!bookingDto.getStart().isBefore(bookingDto.getEnd())) {
            throw new WrongDateBookingException("Wrong start/end of booking");
        }
        if (item.getOwner().getId().equals(ownerId)) {
            throw new OwnerBookingException("OwnerBookingException");
        }
        if (!item.getAvailable()) {
            throw new ItemUnavailableException("Item unavailable");
        }
        bookingDto.setStatus(BookingStatus.WAITING);
        Booking booking = bookingRepository.save(bookingMapper.toBooking(bookingDto, item, user));
        return bookingMapper.toBookingDto(booking);
    }

    @Override
    @Transactional
    public BookingDto editBookingStatus(Long ownerId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new AbsenceException("Booking does not exist"));
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("Access denied");
        }
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new UnsupportedStatusException("Invalid status");
        }
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        return bookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto getById(Long ownerId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new AbsenceException("Booking not exists"));
        userRepository.findById(ownerId).orElseThrow(() -> new AbsenceException("User not exists"));
        if (!booking.getItem().getOwner().getId().equals(ownerId) && !booking.getBooker().getId().equals(ownerId)) {
            throw new AccessDeniedException("Access denied");
        }
        return bookingMapper.toBookingDto(booking);
    }

    @Override
    @Transactional
    public List<BookingDto> getByState(Long bookerId, String state, Integer from, Integer size) {
        PageRequest pageable = PageRequest.of(from > 0 ? from / size : 0, size, sortByStartDesc);
        userRepository.findById(bookerId).orElseThrow(() -> new AbsenceException("User not exists"));
        BookingState bookingState = getBookingState(state);
        Page<Booking> bookingList;
        switch (bookingState) {
            case PAST:
                bookingList = bookingRepository.findByBookerIdAndEndIsBefore(bookerId, LocalDateTime.now(), pageable);
                break;
            case CURRENT:
                bookingList = bookingRepository.findByBookerIdAndEndIsAfterAndStartIsBefore(bookerId,
                        LocalDateTime.now(), LocalDateTime.now(), pageable);
                break;
            case FUTURE:
                bookingList = bookingRepository.findByBookerIdAndStartIsAfter(bookerId, LocalDateTime.now(), pageable);
                break;
            case WAITING:
                bookingList = bookingRepository.findByBookerIdAndStatus(bookerId, BookingStatus.WAITING, pageable);
                break;
            case REJECTED:
                bookingList = bookingRepository.findByBookerIdAndStatus(bookerId, BookingStatus.REJECTED, pageable);
                break;
            case ALL:
                bookingList = bookingRepository.findByBookerId(bookerId, pageable);
                break;
            default:
                throw new UnsupportedStatusException("Unknown state: UNSUPPORTED_STATUS");
        }
        return bookingList.stream().map(bookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<BookingDto> getByStateOwner(Long ownerId, String state, Integer from, Integer size) {
        PageRequest pageable = PageRequest.of(from > 0 ? from / size : 0, size, sortByStartDesc);
        userRepository.findById(ownerId).orElseThrow(() -> new AbsenceException("User not exists"));
        BookingState bookingState = getBookingState(state);
        if (itemRepository.findByOwnerId(ownerId).isEmpty()) {
            throw new ItemUnavailableException("No items found");
        }
        Page<Booking> bookingList;
        switch (bookingState) {
            case PAST:
                bookingList = bookingRepository.findByItemOwnerIdAndEndIsBefore(ownerId,
                        LocalDateTime.now(), pageable);
                break;
            case CURRENT:
                bookingList = bookingRepository.findByItemOwnerIdAndEndIsAfterAndStartIsBefore(ownerId,
                        LocalDateTime.now(), LocalDateTime.now(), pageable);
                break;
            case FUTURE:
                bookingList = bookingRepository.findByItemOwnerIdAndStartIsAfter(ownerId, LocalDateTime.now(), pageable);
                break;
            case WAITING:
                bookingList = bookingRepository.findByItemOwnerIdAndStatus(ownerId, BookingStatus.WAITING, pageable);
                break;
            case REJECTED:
                bookingList = bookingRepository.findByItemOwnerIdAndStatus(ownerId, BookingStatus.REJECTED, pageable);
                break;
            case ALL:
                bookingList = bookingRepository.findByItemOwnerId(ownerId, pageable);
                break;
            default:
                throw new UnsupportedStatusException("Unknown state: UNSUPPORTED_STATUS");
        }
        return bookingList.stream().map(bookingMapper::toBookingDto).collect(Collectors.toList());
    }

    private BookingState getBookingState(String state) {
        try {
            return BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnsupportedStatusException("Unknown state: UNSUPPORTED_STATUS");
        }
    }
}