package ru.practicum.shareit.booking.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.exception.WrongDateBookingException;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {
    private static final String BOOKING_API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("http://localhost:8080") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + BOOKING_API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> getByStateOwner(Long ownerId, BookingState state, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of("status", state.name(), "from", from, "size", size);
        return get("?state={state}&from={from}&size={size}", ownerId, parameters);
    }

    public ResponseEntity<Object> addBooking(Long ownerId, BookItemRequestDto requestDto) {
        return post("", ownerId, requestDto);
    }

    public ResponseEntity<Object> getById(Long ownerId, Long bookingId) {
        return get("/" + bookingId, ownerId, null);
    }

    public ResponseEntity<Object> getByState(Long ownerId, BookingState state, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of("state", state.name(), "from", from, "size", size);
        return get("/owner?state={state}&from={from}&size={size}", ownerId, parameters);
    }

    public ResponseEntity<Object> changeBookingStatus(Long ownerId, Long bookingId, Boolean approved) {
        return patch("/" + bookingId + "?approved=" + approved, ownerId);
    }
}