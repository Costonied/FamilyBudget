package ru.savini.fb.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import ru.savini.fb.domain.entity.AccountingUnit;
import ru.savini.fb.domain.enums.CategoryCode;
import ru.savini.fb.domain.enums.TransactionType;
import ru.savini.fb.gsheets.GSheetsService;
import ru.savini.fb.repo.TransactionRepo;
import ru.savini.fb.domain.entity.Account;
import ru.savini.fb.domain.entity.Category;
import ru.savini.fb.domain.entity.Transaction;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Component
public class TransactionControllerImpl implements TransactionController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionControllerImpl.class);

    private final GSheetsService gSheets;
    private final TransactionRepo transactionRepo;
    private final AccountController accountController;
    private final AccountingUnitController accountingUnitController;

    @Autowired
    public TransactionControllerImpl(
            GSheetsService gSheets,
            TransactionRepo transactionRepo,
            AccountController accountController,
            AccountingUnitController accountingUnitController) {
        this.gSheets = gSheets;
        this.transactionRepo = transactionRepo;
        this.accountController = accountController;
        this.accountingUnitController = accountingUnitController;
    }

    @Override
    public void save(Transaction transaction, Account creditAccount) {
        if (CategoryCode.isGoalsCategory(transaction.getCategory()) ||
                CategoryCode.isTransferCategory(transaction.getCategory())) {
            Transaction creditTransaction = splitAndGetCreditPart(transaction, creditAccount);
            transactionRepo.save(creditTransaction);
            changeAccountAmount(creditTransaction);
            sendTransactionToGoogleSheets(creditTransaction);
        } else {
            setTransactionType(transaction);
        }
        transactionRepo.save(transaction);
        changeAccountAmount(transaction);
        changeAccountingUnitFactAmount(transaction);
        sendTransactionToGoogleSheets(transaction);
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
        if (CategoryCode.isTransferCategory(transaction.getCategory())) {
            return;
        }
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

    private void sendTransactionToGoogleSheets(Transaction transaction) {
        try {
            gSheets.addTransaction(transaction);
        } catch (IOException e) {
            LOGGER.error("Problem save category to Google Sheets");
        }
    }
}
