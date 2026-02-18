package ru.practicum.shareit.exception;

public class InvalidRequestParamException extends RuntimeException {
    public InvalidRequestParamException(String message) {
        super(message);
    }
}
