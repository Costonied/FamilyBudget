package ru.savini.fb.exceptions;

public class NoSuchCurrencyException extends RuntimeException {
    public NoSuchCurrencyException(String message) {
        super(message);
    }
}
