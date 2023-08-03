package ru.practicum.shareit.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.booking.exception.UnsupportedStatusException;

@RestControllerAdvice
@Slf4j
public class ExceptionsHandler {
    @ExceptionHandler(UnsupportedStatusException.class)
    public ResponseEntity<String> handleUnsupportedStatusException(UnsupportedStatusException ex) {
        return new ResponseEntity<>("Unknown state: UNSUPPORTED_STATUS", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}