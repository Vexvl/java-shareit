package ru.practicum.shareit.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.booking.exception.InvalidStatusException;
import ru.practicum.shareit.booking.exception.NoBookingException;
import ru.practicum.shareit.booking.exception.UnknownStatusException;
import ru.practicum.shareit.booking.exception.WrongDateBookingException;
import ru.practicum.shareit.item.exception.*;
import ru.practicum.shareit.user.exception.EmailDuplicateException;
import ru.practicum.shareit.user.exception.InvalidRequestException;

import java.util.Map;

@RestControllerAdvice
public class ExceptionsHandler {
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

    @ExceptionHandler(NoPossessionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleNoPossessionException(NoPossessionException e) {
        return Map.of("NoPossessionException", e.getMessage());
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

    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidRequestException(InvalidRequestException e) {
        return Map.of("InvalidRequestException", e.getMessage());
    }

    @ExceptionHandler(EmailDuplicateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleEmailDuplicateException(EmailDuplicateException e) {
        return Map.of("EmailDuplicateException", e.getMessage());
    }

    @ExceptionHandler(UnknownStatusException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleUnknownStatusException(UnknownStatusException e) {
        return Map.of("error", "Unknown state: UNSUPPORTED_STATUS", "errorMessage", "Ошибка");
    }

    @ExceptionHandler(NoBookingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleNoBookingException(NoBookingException e) {
        return Map.of("NoBookingException", e.getMessage());
    }

    @ExceptionHandler(InvalidStatusException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidStatusException(InvalidStatusException e) {
        return Map.of("InvalidStatusException", e.getMessage());
    }
}