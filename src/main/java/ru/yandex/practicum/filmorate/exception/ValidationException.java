package ru.yandex.practicum.filmorate.exception;

public class ValidationException extends RuntimeException {
    // Конструктор с сообщением об ошибке
    public ValidationException(String message) {
        super(message);
    }

    // Конструктор с сообщением и причиной ошибки
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}