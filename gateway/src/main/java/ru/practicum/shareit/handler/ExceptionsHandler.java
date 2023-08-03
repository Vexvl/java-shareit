package ru.practicum.shareit.handler;

import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.booking.exception.WrongDateBookingException;

import java.util.Map;

@RestControllerAdvice
public class ExceptionsHandler {

    /*@ExceptionHandler(WrongDateBookingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleWrongDateBookingException(WrongDateBookingException e) {
        return Map.of("WrongDateBookingException", e.getMessage());
    }*/
}