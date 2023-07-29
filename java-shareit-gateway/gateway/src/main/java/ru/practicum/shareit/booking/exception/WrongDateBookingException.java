package src.main.java.ru.practicum.shareit.booking.exception;

public class WrongDateBookingException extends RuntimeException {
    public WrongDateBookingException(String message) {
        super(message);
    }
}