package ru.savini.fb.controller;

import org.apache.commons.lang3.StringUtils;
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
import ru.savini.fb.exceptions.InvalidCategoryCodeException;
import ru.savini.fb.exceptions.NoSuchTransactionIdException;
import ru.savini.fb.repo.TransactionRepo;
import ru.savini.fb.repo.TransactionPairRepo;
import ru.savini.fb.domain.entity.Account;
import ru.savini.fb.domain.entity.Category;
import ru.savini.fb.domain.entity.Transaction;
import ru.savini.fb.domain.entity.TransactionEvent;
import ru.savini.fb.repo.filters.TransactionFilter;

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
    public void save(TransactionEvent transactionEvent) {
        Transaction debitTransaction;
        Transaction creditTransaction;
        Transaction baseTransaction = getBaseTransactionFromEvent(transactionEvent);

        if (isPairedTransaction(transactionEvent.getCategory())) {
            debitTransaction = new Transaction(baseTransaction, transactionEvent.getDebitAccount(), TransactionType.DEBIT.getType());
            creditTransaction = new Transaction(baseTransaction, transactionEvent.getCreditAccount(), TransactionType.CREDIT.getType());
            processAndSavePairedTransactions(debitTransaction, creditTransaction);
        }
        else if (CategoryCode.isIncomeCategory(transactionEvent.getCategory())) {
            creditTransaction = new Transaction(baseTransaction, transactionEvent.getCreditAccount(), TransactionType.CREDIT.getType());
            processAndSaveTransaction(creditTransaction);
        }
        else if (CategoryCode.isOutgoingCategory(transactionEvent.getCategory()) ||
                CategoryCode.isWithdrawalCategory(transactionEvent.getCategory())) {
            debitTransaction = new Transaction(baseTransaction, transactionEvent.getDebitAccount(), TransactionType.DEBIT.getType());
            processAndSaveTransaction(debitTransaction);
        }
        else {
            LOGGER.error("Not recognize category code [{}] while saving transaction", transactionEvent.getCategory().getType());
            throw new InvalidCategoryCodeException();
        }
    }

    private void processAndSaveTransaction(Transaction transaction) {
        // check does it transaction edit
        if (transaction.getId() != null) {
            Transaction originalTransaction = transactionRepo.findById(transaction.getId())
                    .orElseThrow(NoSuchTransactionIdException::new);
            changeAccountAmount(originalTransaction, transaction);
            if (transaction.getAccount().isNeedAccounting()) {
                changeAccountingUnitFactAmount(originalTransaction, transaction);
            }
        } else {
            changeAccountAmount(transaction);
            if (transaction.getAccount().isNeedAccounting()) {
                changeAccountingUnitFactAmount(transaction);
            }
        }
        transactionRepo.save(transaction);
    }

    private Transaction getBaseTransactionFromEvent(TransactionEvent event) {
        Transaction baseTransaction = new Transaction();
        baseTransaction.setCategory(event.getCategory());
        baseTransaction.setAmount(event.getAmount());
        baseTransaction.setComment(event.getComment());
        baseTransaction.setDate(event.getDate());
        baseTransaction.setId(event.getId());
        return baseTransaction;
    }

    private boolean isPairedTransaction(Category transactionCategory) {
        return CategoryCode.isGoalsCategory(transactionCategory) ||
                CategoryCode.isTransferCategory(transactionCategory);
    }

    @Override
    public void delete(TransactionEvent transactionEvent) {
        transactionRepo.deleteById(transactionEvent.getId());
    }

    @Override
    public List<Transaction> getAllByOrderByDateDesc() {
        return transactionRepo.getAllByOrderByDateDesc();
    }

    @Override
    public List<Transaction> getFiltered(TransactionFilter filter) {
        if (StringUtils.isNotEmpty(filter.getAccountName())) {
            return transactionRepo.findAllByAccount_NameContainsIgnoreCaseOrderByDateDesc(filter.getAccountName());
        } else {
            return getAllByOrderByDateDesc();
        }
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
        LocalDate editedTransDate = editedTrans.getDate();
        Category originalTransCategory = originalTrans.getCategory();
        Category editedTransCategory = editedTrans.getCategory();

        AccountingUnit originalAccountingUnit = accountingUnitController
                .getByCategoryAndLocalDate(originalTransCategory, originalTransDate);
        AccountingUnit editedAccountingUnit = accountingUnitController
                .getByCategoryAndLocalDate(editedTransCategory, editedTransDate);
        accountingUnitController.decreaseFactAmount(originalAccountingUnit, originalTransMoney);
        accountingUnitController.increaseFactAmount(editedAccountingUnit, editedTransMoney);
    }

    private boolean isCreditTransaction(Transaction transaction) {
        return TransactionType.CREDIT.getType().equalsIgnoreCase(transaction.getType());
    }

    private boolean isAccountSame(Transaction firstTrans, Transaction secondTrans) {
        return firstTrans.getAccount().getId().equals(secondTrans.getAccount().getId());
    }

    private void processAndSavePairedTransactions(Transaction debitTransaction, Transaction creditTransaction) {
        // check does it transaction edit
        if (debitTransaction.getId() != null) {
            TransactionPair transactionPair;
            Transaction originalPairTransaction;
            long originalEditedTransactionId = debitTransaction.getId();
            Transaction originalEditedTransaction = transactionRepo.getOne(originalEditedTransactionId);
            String originalEditedTransactionType = originalEditedTransaction.getType();
            if (TransactionType.CREDIT.getType().equals(originalEditedTransactionType)) {
                transactionPair = transactionPairRepo.getTransactionPairByCreditTransaction(originalEditedTransaction);
                originalPairTransaction = transactionPair.getDebitTransaction();
                debitTransaction.setId(originalPairTransaction.getId());
                debitTransaction.setAccount(originalPairTransaction.getAccount());
                debitTransaction.setAmount(creditTransaction.getAmount());
                changeAccountAmount(originalEditedTransaction, creditTransaction);
                changeAccountAmount(originalPairTransaction, debitTransaction);
                changeAccountingUnitFactAmount(originalPairTransaction, debitTransaction);
            } else {
                transactionPair = transactionPairRepo.getTransactionPairByDebitTransaction(originalEditedTransaction);
                originalPairTransaction = transactionPair.getCreditTransaction();
                creditTransaction.setId(originalPairTransaction.getId());
                creditTransaction.setAccount(originalPairTransaction.getAccount());
                creditTransaction.setAmount(debitTransaction.getAmount());
                changeAccountAmount(originalEditedTransaction, debitTransaction);
                changeAccountAmount(originalPairTransaction, creditTransaction);
                changeAccountingUnitFactAmount(originalEditedTransaction, debitTransaction);
            }
        } else {
            changeAccountAmount(debitTransaction);
            changeAccountAmount(creditTransaction);
            changeAccountingUnitFactAmount(debitTransaction);
            TransactionPair transactionPair = new TransactionPair(debitTransaction, creditTransaction);
            transactionPairRepo.save(transactionPair);
        }
        transactionRepo.save(debitTransaction);
        transactionRepo.save(creditTransaction);
    }

    @Override
    public TransactionEvent getEventFromTransaction(Transaction transaction) {
        if (transaction == null) {
            return null;
        } else {
            return new TransactionEvent(transaction);
        }
    }
}
