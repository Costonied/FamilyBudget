package ru.savini.fb.controller;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ru.savini.fb.domain.entity.AccountingUnit;
import ru.savini.fb.domain.entity.TransactionPair;
import ru.savini.fb.domain.enums.CategoryCode;
import ru.savini.fb.domain.enums.TransactionType;
import ru.savini.fb.repo.TransactionRepo;
import ru.savini.fb.repo.TransactionPairRepo;
import ru.savini.fb.domain.entity.Account;
import ru.savini.fb.domain.entity.Category;
import ru.savini.fb.domain.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class TransactionControllerImpl implements TransactionController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionControllerImpl.class);

    private final TransactionRepo transactionRepo;
    private final TransactionPairRepo transactionPairRepo;
    private final AccountController accountController;
    private final AccountingUnitController accountingUnitController;

    @Autowired
    public TransactionControllerImpl(
            TransactionRepo transactionRepo,
            TransactionPairRepo transactionPairRepo,
            AccountController accountController,
            AccountingUnitController accountingUnitController) {
        this.transactionRepo = transactionRepo;
        this.transactionPairRepo = transactionPairRepo;
        this.accountController = accountController;
        this.accountingUnitController = accountingUnitController;
    }

    @Transactional
    @Override
    public void save(Transaction transaction, Account creditAccount) {
        if (isSplittedTransaction(transaction)) {
            processAndSaveSplittedTransactions(transaction, creditAccount);
            return;
        }
        // check does it transaction edit
        if (transaction.getId() != null) {
            Transaction originalTransaction = transactionRepo.getOne(transaction.getId());
            changeAccountAmount(originalTransaction, transaction);
            changeAccountingUnitFactAmount(originalTransaction, transaction);
        } else {
            setTransactionType(transaction);
            setCorrectAccount(transaction, creditAccount);
            changeAccountAmount(transaction);
            changeAccountingUnitFactAmount(transaction);
        }
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

    private void changeAccountAmount(Transaction transaction) {
        BigDecimal transAmount = transaction.getAmount();
        Account transAccount = transaction.getAccount();
        if (isCreditTransaction(transaction)) {
            accountController.putMoney(transAmount, transAccount);
        } else {
            accountController.withdrawMoney(transAmount, transAccount);
        }
    }

    private void changeAccountAmount(Transaction originalTrans, Transaction editedTrans) {
        if (isAccountSame(originalTrans, editedTrans)) {
            Money editedTransMoney = Money.of(getCurrencyUnitFromTransaction(editedTrans), editedTrans.getAmount());
            Money originalTransMoney = Money.of(getCurrencyUnitFromTransaction(originalTrans), originalTrans.getAmount());
            Money diffAmount = editedTransMoney.minus(originalTransMoney);
            if (isCreditTransaction(originalTrans)) {
                accountController.putMoney(diffAmount.getAmount(), originalTrans.getAccount());
            } else {
                accountController.withdrawMoney(diffAmount.getAmount(), originalTrans.getAccount());
            }
        } else {
            if (isCreditTransaction(originalTrans)) {
                accountController.withdrawMoney(originalTrans.getAmount(), originalTrans.getAccount());
                accountController.putMoney(editedTrans.getAmount(), editedTrans.getAccount());
            } else {
                accountController.putMoney(originalTrans.getAmount(), originalTrans.getAccount());
                accountController.withdrawMoney(editedTrans.getAmount(), editedTrans.getAccount());
            }
        }
    }

    private CurrencyUnit getCurrencyUnitFromTransaction(Transaction transaction) {
        return CurrencyUnit.of(transaction.getAccount().getCurrency());
    }

    private void changeAccountingUnitFactAmount(Transaction transaction) {
        if (CategoryCode.isTransferCategory(transaction.getCategory())) {
            return;
        }
        Money transMoney = Money.of(getCurrencyUnitFromTransaction(transaction), transaction.getAmount());
        LocalDate transDate = transaction.getDate();
        Category transCategory = transaction.getCategory();

        AccountingUnit accountingUnit = accountingUnitController
                .getByCategoryAndLocalDate(transCategory, transDate);
        accountingUnitController.increaseFactAmount(accountingUnit, transMoney);
    }

    private void changeAccountingUnitFactAmount(Transaction originalTrans, Transaction editedTrans) {
        if (CategoryCode.isTransferCategory(originalTrans.getCategory())) {
            return;
        }
        Money originalTransMoney = Money.of(getCurrencyUnitFromTransaction(originalTrans), originalTrans.getAmount());
        Money editedTransMoney = Money.of(getCurrencyUnitFromTransaction(editedTrans), editedTrans.getAmount());
        LocalDate originalTransDate = originalTrans.getDate();
        LocalDate editedTransDate = originalTrans.getDate();
        Category transCategory = originalTrans.getCategory();

        AccountingUnit originalAccountingUnit = accountingUnitController
                .getByCategoryAndLocalDate(transCategory, originalTransDate);
        AccountingUnit editedAccountingUnit = accountingUnitController
                .getByCategoryAndLocalDate(transCategory, editedTransDate);
        accountingUnitController.decreaseFactAmount(originalAccountingUnit, originalTransMoney);
        accountingUnitController.decreaseFactAmount(editedAccountingUnit, editedTransMoney);
    }

    private void setTransactionType(Transaction transaction) {
        String transCategoryType = transaction.getCategory().getType();
        if (transCategoryType.equalsIgnoreCase(CategoryCode.OUTGO.getCode())) {
            transaction.setType(TransactionType.DEBIT.getType());
        } else if (transCategoryType.equalsIgnoreCase(CategoryCode.INCOME.getCode())) {
            transaction.setType(TransactionType.CREDIT.getType());
        }
    }

    private void setCorrectAccount(Transaction transaction, Account creditAccount) {
        if (CategoryCode.isIncomeCategory(transaction.getCategory())) {
            transaction.setAccount(creditAccount);
        }
    }

    private Transaction splitAndGetCreditPart(Transaction transaction, Account creditAccount) {
        transaction.setType(TransactionType.DEBIT.getType());
        Transaction creditTransaction = new Transaction(transaction);
        creditTransaction.setAccount(creditAccount);
        creditTransaction.setType(TransactionType.CREDIT.getType());
        return creditTransaction;
    }

    private boolean isSplittedTransaction(Transaction transaction) {
        return CategoryCode.isGoalsCategory(transaction.getCategory()) ||
                CategoryCode.isTransferCategory(transaction.getCategory());
    }

    private boolean isCreditTransaction(Transaction transaction) {
        return TransactionType.CREDIT.getType().equalsIgnoreCase(transaction.getType());
    }

    private boolean isAccountSame(Transaction firstTrans, Transaction secondTrans) {
        return firstTrans.getAccount().getId().equals(secondTrans.getAccount().getId());
    }

    private void processAndSaveSplittedTransactions(Transaction debitTransaction, Account creditAccount) {
        Transaction creditTransaction = splitAndGetCreditPart(debitTransaction, creditAccount);
        TransactionPair transactionPair = new TransactionPair(debitTransaction, creditTransaction);
        transactionRepo.save(debitTransaction);
        transactionRepo.save(creditTransaction);
        transactionPairRepo.save(transactionPair);
        changeAccountAmount(debitTransaction);
        changeAccountAmount(creditTransaction);
        changeAccountingUnitFactAmount(debitTransaction);
    }
}
