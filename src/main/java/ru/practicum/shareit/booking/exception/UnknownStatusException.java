package ru.practicum.shareit.booking.exception;

public class UnknownStatusException extends RuntimeException {
    public UnknownStatusException(String message) {
        super(message);
    }
}