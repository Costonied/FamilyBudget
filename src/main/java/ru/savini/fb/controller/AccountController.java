package ru.savini.fb.controller;

import java.math.BigDecimal;
import java.util.List;
import ru.savini.fb.domain.entity.Account;

public interface AccountController {
    void save(Account account);
    void delete(Account account);

    void putMoney(BigDecimal moneyAmount, Account account);
    void withdrawMoney(BigDecimal moneyAmount, Account account);

    Account getById(long accountId);
    List<Account> getAll();
    List<Account> getByNameContainsIgnoreCase(String name);
    List<Account> getAllByNeedAccountingIsTrue();
    List<Account> getAllByNeedAccountingIsFalse();
}
