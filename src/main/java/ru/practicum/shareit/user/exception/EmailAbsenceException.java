package ru.practicum.shareit.user.exception;

public class EmailAbsenceException extends RuntimeException {
    public EmailAbsenceException(String message) {
        super(message);
    }
}
