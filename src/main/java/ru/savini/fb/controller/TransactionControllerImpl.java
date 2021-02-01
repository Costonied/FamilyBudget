package ru.savini.fb.controller;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import ru.savini.fb.repo.TransactionRepo;
import ru.savini.fb.domain.entity.Account;
import ru.savini.fb.domain.entity.Category;
import ru.savini.fb.domain.entity.Transaction;

import java.util.List;

@Component
public class TransactionControllerImpl implements TransactionController {
    private final TransactionRepo transactionRepo;
    private final AccountController accountController;
    private final CategoryController categoryController;

    @Autowired
    public TransactionControllerImpl(
            TransactionRepo transactionRepo,
            AccountController accountController,
            CategoryController categoryController) {
        this.transactionRepo = transactionRepo;
        this.accountController = accountController;
        this.categoryController = categoryController;
    }

    @Override
    public void save(Transaction transaction) {
        transactionRepo.save(transaction);
        changeAccountAmount(transaction);
    }

    @Override
    public void delete(Transaction transaction) {
        transactionRepo.delete(transaction);
    }

    @Override
    public List<Transaction> getAll() {
        return transactionRepo.findAll();
    }

    private void changeAccountAmount(Transaction transaction) {
        Category category = transaction.getCategory();
        double transAmount = transaction.getAmount();
        Account transAccount = transaction.getAccount();

        if (categoryController.isIncome(category)) {
            accountController.putMoney(transAmount, transAccount);
        } else if (categoryController.isOutgoing(category)) {
            accountController.withdrawMoney(transAmount, transAccount);
        }
    }
}
