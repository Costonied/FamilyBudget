package ru.savini.fb.controller;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import ru.savini.fb.domain.entity.AccountingUnit;
import ru.savini.fb.repo.TransactionRepo;
import ru.savini.fb.domain.entity.Account;
import ru.savini.fb.domain.entity.Category;
import ru.savini.fb.domain.entity.Transaction;

import java.time.LocalDate;
import java.util.List;

@Component
public class TransactionControllerImpl implements TransactionController {
    private final TransactionRepo transactionRepo;
    private final AccountController accountController;
    private final CategoryController categoryController;
    private final AccountingUnitController accountingUnitController;

    @Autowired
    public TransactionControllerImpl(
            TransactionRepo transactionRepo,
            AccountController accountController,
            CategoryController categoryController,
            AccountingUnitController accountingUnitController) {
        this.transactionRepo = transactionRepo;
        this.accountController = accountController;
        this.categoryController = categoryController;
        this.accountingUnitController = accountingUnitController;
    }

    @Override
    public void save(Transaction transaction) {
        transactionRepo.save(transaction);
        changeAccountAmount(transaction);
        changeAccountingUnitFactAmount(transaction);
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

    private void changeAccountingUnitFactAmount(Transaction transaction) {
        double transAmount = transaction.getAmount();
        LocalDate transDate = transaction.getDate();
        Category transCategory = transaction.getCategory();

        AccountingUnit accountingUnit = accountingUnitController
                .getByCategoryAndLocalDate(transCategory, transDate);
        accountingUnitController.increaseFactAmount(accountingUnit, transAmount);
    }
}
