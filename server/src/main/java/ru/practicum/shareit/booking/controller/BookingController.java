package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
@Validated
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingDto addBooking(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                 @RequestBody @Valid BookingDto bookingDto) {
        return bookingService.addBooking(bookingDto, ownerId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto changeBookingStatus(@RequestHeader("X-Sharer-User-Id") Long ownerId, @PathVariable Long bookingId,
                                          @RequestParam Boolean approved) {
        return bookingService.editBookingStatus(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getById(@RequestHeader("X-Sharer-User-Id") Long ownerId, @PathVariable Long bookingId) {
        return bookingService.getById(ownerId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getByState(@RequestHeader("X-Sharer-User-Id") Long bookerId,
                                       @RequestParam(defaultValue = "ALL") String state,
                                       @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer from,
                                       @RequestParam(required = false, defaultValue = "10") @Positive Integer size) {
        return bookingService.getByState(bookerId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> getByStateOwner(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                            @RequestParam(defaultValue = "ALL") String state,
                                            @RequestParam(required = false, defaultValue = "0")
                                            @PositiveOrZero Integer from,
                                            @RequestParam(required = false, defaultValue = "10")
                                            @Positive Integer size) {
        return bookingService.getByStateOwner(ownerId, state, from, size);
    }
}