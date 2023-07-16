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
        return Map.of("error", "Такого элемента нет", "errorMessage", "Ошибка на сервере");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleAccessDeniedException(AccessDeniedException e) {
        return Map.of("error", "Вещь не принадлежит user", "errorMessage", "Ошибка на сервере");
    }

    @ExceptionHandler(NoPossessionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleNoPossessionException(NoPossessionException e) {
        return Map.of("error", "NoPossessionException", "errorMessage", "Ошибка на сервере");
    }

    @ExceptionHandler(ItemUnavailableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleItemUnavailableException(ItemUnavailableException e) {
        return Map.of("error", "ItemUnavailableException", "errorMessage", "Ошибка на сервере");
    }

    @ExceptionHandler(OwnerBookingException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleOwnerBookingException(OwnerBookingException e) {
        return Map.of("error", "OwnerBookingException", "errorMessage", "Ошибка на сервере");
    }

    @ExceptionHandler(WrongDateBookingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleAccessDeniedException(WrongDateBookingException e) {
        return Map.of("error", "WrongDateBookingException", "errorMessage", "Ошибка на сервере");
    }

    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidRequestException(InvalidRequestException e) {
        return Map.of("error", "InvalidRequestException", "errorMessage", "Ошибка на сервере");
    }

    @ExceptionHandler(EmailDuplicateException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleEmailDuplicateException(EmailDuplicateException e) {
        return Map.of("error", "EmailDuplicateException", "errorMessage", "Ошибка на сервере");
    }

    @ExceptionHandler(UnknownStatusException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleUnknownStatusException(UnknownStatusException e) {
        return Map.of("error", "Unknown state: UNSUPPORTED_STATUS", "errorMessage", "Ошибка на сервере");
    }

    @ExceptionHandler(NoBookingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleEmailDuplicateException(NoBookingException e) {
        return Map.of("error", "NoBookingException", "errorMessage", "Ошибка на сервере");
    }

    @ExceptionHandler(InvalidStatusException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidStatusException(InvalidStatusException e) {
        return Map.of("error", "InvalidStatusException", "errorMessage", "Ошибка на сервере");
    }
}