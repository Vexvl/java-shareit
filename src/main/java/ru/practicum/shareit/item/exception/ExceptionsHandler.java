package ru.practicum.shareit.item.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
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

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleThrowable(Throwable t) {
        return Map.of("error", "Возникло непредвиденное исключение", "errorMessage", "Ошибка на сервере");
    }

    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidRequestException(InvalidRequestException e) {
        return Map.of("error", "BAD_REQUEST", "errorMessage", "Ошибка на сервере");
    }
}