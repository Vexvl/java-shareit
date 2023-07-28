package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto addBooking(BookingDto bookingDto, Long ownerId);

    BookingDto editBookingStatus(Long ownerId, Long bookingId, Boolean approved);

    BookingDto getById(Long ownerId, Long bookingId);

    List<BookingDto> getByState(Long bookerId, String state, Integer from, Integer size);

    List<BookingDto> getByStateOwner(Long ownerId, String state, Integer from, Integer size);
}