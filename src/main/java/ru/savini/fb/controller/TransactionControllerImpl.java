package ru.savini.fb.controller;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import ru.savini.fb.repo.TransactionRepo;
import ru.savini.fb.domain.entity.Transaction;

import java.util.List;

@Component
public class TransactionControllerImpl implements TransactionController {
    private final TransactionRepo transactionRepo;

    @Autowired
    public TransactionControllerImpl(TransactionRepo transactionRepo) {
        this.transactionRepo = transactionRepo;
    }

    @Override
    public void save(Transaction transaction) {
        transactionRepo.save(transaction);
    }

    @Override
    public void delete(Transaction transaction) {
        transactionRepo.delete(transaction);
    }

    @Override
    public List<Transaction> getAll() {
        return transactionRepo.findAll();
    }
}
