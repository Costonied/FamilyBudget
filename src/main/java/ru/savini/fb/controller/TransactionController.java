package ru.savini.fb.controller;

import java.util.List;

import ru.savini.fb.domain.entity.Transaction;
import ru.savini.fb.domain.entity.TransactionEvent;

public interface TransactionController {
    void save(TransactionEvent transactionEvent);
    void delete(TransactionEvent transaction);
    List<Transaction> getAll();
    TransactionEvent getEventFromTransaction(Transaction transaction);
}
