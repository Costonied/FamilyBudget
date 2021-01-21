package ru.savini.fb.exceptions;

public class NoSuchAccountIdException extends RuntimeException {
    public NoSuchAccountIdException(String message) {
        super(message);
    }
}
