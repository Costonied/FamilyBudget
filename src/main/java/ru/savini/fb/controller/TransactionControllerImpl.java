package ru.savini.fb.controller;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import ru.savini.fb.domain.entity.AccountingUnit;
import ru.savini.fb.domain.enums.CategoryCode;
import ru.savini.fb.domain.enums.TransactionType;
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
    private final AccountingUnitController accountingUnitController;

    @Autowired
    public TransactionControllerImpl(
            TransactionRepo transactionRepo,
            AccountController accountController,
            AccountingUnitController accountingUnitController) {
        this.transactionRepo = transactionRepo;
        this.accountController = accountController;
        this.accountingUnitController = accountingUnitController;
    }

    @Override
    public void save(Transaction transaction, Account creditAccount) {
        if (CategoryCode.isGoalsCategory(transaction.getCategory())) {
            Transaction creditTransaction = splitAndGetCreditPart(transaction, creditAccount);
            transactionRepo.save(creditTransaction);
            changeAccountAmount(creditTransaction);
        } else {
            setTransactionType(transaction);
        }
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
        double transAmount = transaction.getAmount();
        Account transAccount = transaction.getAccount();

        if (TransactionType.CREDIT.getType().equalsIgnoreCase(transaction.getType())) {
            accountController.putMoney(transAmount, transAccount);
        } else if (TransactionType.DEBIT.getType().equalsIgnoreCase(transaction.getType())) {
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

    private void setTransactionType(Transaction transaction) {
        String transCategoryType = transaction.getCategory().getType();
        if (transCategoryType.equalsIgnoreCase(CategoryCode.OUTGO.getCode())) {
            transaction.setType(TransactionType.DEBIT.getType());
        } else if (transCategoryType.equalsIgnoreCase(CategoryCode.INCOME.getCode())) {
            transaction.setType(TransactionType.CREDIT.getType());
        }
    }

    private Transaction splitAndGetCreditPart(Transaction transaction, Account creditAccount) {
        transaction.setType(TransactionType.DEBIT.getType());
        Transaction creditTransaction = new Transaction(transaction);
        creditTransaction.setAccount(creditAccount);
        creditTransaction.setType(TransactionType.CREDIT.getType());
        return creditTransaction;
    }
}
