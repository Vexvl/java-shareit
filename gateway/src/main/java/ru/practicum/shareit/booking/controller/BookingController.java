package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
	private final BookingClient bookingClient;

	@GetMapping
	public ResponseEntity<Object> getUserBookingList(@RequestHeader("X-Sharer-User-Id") Long ownerId,
													 @RequestParam(name = "state", defaultValue = "all") String stateParam,
													 @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
													 @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
		return bookingClient.getBookings(ownerId, state, from, size);
	}

	@PostMapping
	public ResponseEntity<Object> addBooking(@RequestHeader("X-Sharer-User-Id") Long ownerId,
											 @RequestBody @Valid BookItemRequestDto requestDto) {
		return bookingClient.addBooking(ownerId, requestDto);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> getById(@RequestHeader("X-Sharer-User-Id") Long ownerId,
										  @PathVariable Long bookingId) {
		return bookingClient.getById(ownerId, bookingId);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> getUserItemsBooking(@RequestHeader("X-Sharer-User-Id") Long ownerId,
													  @RequestParam(name = "state", defaultValue = "all") String stateParam,
													  @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer from,
													  @RequestParam(required = false, defaultValue = "10") @Positive Integer size) {
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
		return bookingClient.getUserItemsBooking(ownerId, state, from, size);
	}

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> editBookingStatus(@RequestHeader("X-Sharer-User-Id") Long ownerId,
													@PathVariable Long bookingId,
													@RequestParam String approved) {
		return bookingClient.editBookingStatus(ownerId, bookingId, approved);
	}
}