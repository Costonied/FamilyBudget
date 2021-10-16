package ru.savini.fb.controller;

import java.util.List;

import ru.savini.fb.domain.entity.Transaction;
import ru.savini.fb.domain.entity.TransactionEvent;
import ru.savini.fb.repo.filters.TransactionFilter;

public interface TransactionController {
    void save(TransactionEvent transactionEvent);
    void delete(TransactionEvent transaction);
    List<Transaction> getAllByOrderByDateDesc();
    List<Transaction> getFiltered(TransactionFilter filter);
    TransactionEvent getEventFromTransaction(Transaction transaction);
}
