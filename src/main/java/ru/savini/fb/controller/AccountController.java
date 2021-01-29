package ru.savini.fb.controller;

import java.util.List;
import ru.savini.fb.domain.entity.Account;

public interface AccountController {
    void save(Account account);
    void delete(Account account);

    void putMoney(double moneyAmount, Account account);
    void withdrawMoney(double moneyAmount, Account account);

    Account getById(long accountId);
    List<Account> getAll();
    List<Account> getByNameStartsWithIgnoreCase(String name);
}
