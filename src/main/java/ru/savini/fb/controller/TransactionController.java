package ru.savini.fb.controller;

import java.util.List;

import ru.savini.fb.domain.entity.Account;
import ru.savini.fb.domain.entity.Transaction;

public interface TransactionController {
    void save(Transaction transaction, Account creditAccount);
    void delete(Transaction transaction);
    List<Transaction> getAll();
}
