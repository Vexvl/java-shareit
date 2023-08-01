package ru.practicum.shareit.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.booking.exception.NoBookingException;
import ru.practicum.shareit.booking.exception.UnsupportedStatusException;
import ru.practicum.shareit.booking.exception.WrongDateBookingException;
import ru.practicum.shareit.item.exception.*;
import ru.practicum.shareit.user.exception.EmailDuplicateException;

import java.util.Map;

@RestControllerAdvice
public class ExceptionsHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnhandledException(Exception e) {
        return new ResponseEntity<>("An unexpected error occurred", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AbsenceException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleAbsenceException(AbsenceException e) {
        return Map.of("Такого элемента нет", e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleAccessDeniedException(AccessDeniedException e) {
        return Map.of("Вещь не принадлежит user", e.getMessage());
    }

    @ExceptionHandler(ItemUnavailableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleItemUnavailableException(ItemUnavailableException e) {
        return Map.of("ItemUnavailableException", e.getMessage());
    }

    @ExceptionHandler(OwnerBookingException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleOwnerBookingException(OwnerBookingException e) {
        return Map.of("OwnerBookingException", e.getMessage());
    }

    @ExceptionHandler(WrongDateBookingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleAccessDeniedException(WrongDateBookingException e) {
        return Map.of("WrongDateBookingException", e.getMessage());
    }

    @ExceptionHandler(EmailDuplicateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleEmailDuplicateException(EmailDuplicateException e) {
        return Map.of("EmailDuplicateException", e.getMessage());
    }

    @ExceptionHandler(NoBookingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleNoBookingException(NoBookingException e) {
        return Map.of("NoBookingException", e.getMessage());
    }
}