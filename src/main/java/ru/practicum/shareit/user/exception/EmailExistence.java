package ru.practicum.shareit.user.exception;

public class EmailExistence extends RuntimeException {
    public EmailExistence(String message) {
        super(message);
    }
}
