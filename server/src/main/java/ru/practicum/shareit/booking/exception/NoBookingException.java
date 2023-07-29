package src.main.java.ru.practicum.shareit.booking.exception;

public class NoBookingException extends RuntimeException {
    public NoBookingException(String message) {
        super(message);
    }
}