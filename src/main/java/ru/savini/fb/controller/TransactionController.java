package ru.savini.fb.controller;

import java.util.List;

import ru.savini.fb.domain.entity.Transaction;

public interface TransactionController {
    void save(Transaction transaction);
    void delete(Transaction transaction);
    List<Transaction> getAll();
}
