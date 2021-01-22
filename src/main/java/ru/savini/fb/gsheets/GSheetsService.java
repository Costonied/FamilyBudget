package ru.savini.fb.gsheets;

import ru.savini.fb.domain.entity.Account;
import ru.savini.fb.domain.entity.AccountingUnit;
import ru.savini.fb.domain.entity.Category;
import ru.savini.fb.domain.entity.Transaction;

import java.io.IOException;
import java.util.List;

public interface GSheetsService {
    List<Account> getAccounts() throws IOException;
    void addAccount(Account account) throws IOException;
    void addCategory(Category category) throws IOException;
    void addTransaction(Transaction transaction) throws IOException;
    void addAccountingUnit(AccountingUnit accountingUnit) throws IOException;
}
