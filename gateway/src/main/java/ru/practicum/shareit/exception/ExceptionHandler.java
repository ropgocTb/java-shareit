package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ExceptionHandler {
     @org.springframework.web.bind.annotation.ExceptionHandler
     @ResponseStatus(HttpStatus.BAD_REQUEST)
     public Map<String, String> handleValidationException(final ValidationException e) {
         return Map.of(
                 "error", "Ошибка валидации",
                 "errorMessage", e.getMessage()
         );
     }
}