package ru.savini.fb.exceptions;

public class InvalidCalculationOperationType extends RuntimeException {
    public InvalidCalculationOperationType(String message) {
        super(message);
    }
}
