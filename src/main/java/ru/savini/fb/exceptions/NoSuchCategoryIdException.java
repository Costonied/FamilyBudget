package ru.savini.fb.exceptions;

public class NoSuchCategoryIdException extends RuntimeException {
    public NoSuchCategoryIdException(String message) {
        super(message);
    }
}
