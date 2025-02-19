package ru.yandex.practicum.catsgram.exception;

public class DublicatedDataException extends RuntimeException {
    public DublicatedDataException(String message) {
        super(message);
    }
}
