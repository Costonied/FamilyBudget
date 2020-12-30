package ru.savini.fb.gsheets;

import ru.savini.fb.domain.entity.Account;

import java.io.IOException;
import java.util.List;

public interface GSheetsService {
    List<Account> getAccounts() throws IOException;
    void addAccount(Account account) throws IOException;
}
