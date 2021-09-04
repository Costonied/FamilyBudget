package ru.savini.fb.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.joda.money.Money;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import ru.savini.fb.repo.AccountRepo;
import ru.savini.fb.domain.entity.Account;
import ru.savini.fb.exceptions.NoSuchAccountIdException;

@Component
public class AccountControllerImpl implements AccountController {
    private final AccountRepo repository;

    @Autowired
    public AccountControllerImpl(AccountRepo repository) {
        this.repository = repository;
    }

    @Override
    public void save(Account account) {
        repository.save(account);
    }

    @Override
    public void delete(Account account) {
        repository.delete(account);
    }

    @Override
    public void putMoney(BigDecimal transactionAmount, Account account) {
        Money currentAccountMoney = account.getMoney();
        Money transactionMoney = Money.of(currentAccountMoney.getCurrencyUnit(), transactionAmount);
        Money newAccountMoney = currentAccountMoney.plus(transactionMoney);
        account.setMoney(newAccountMoney);
        account.setAmount(newAccountMoney.getAmount());
        this.save(account);
    }

    // TODO: refactoring needed - delete duplicate of code
    @Override
    public void withdrawMoney(BigDecimal transactionAmount, Account account) {
        Money currentAccountMoney = account.getMoney();
        Money transactionMoney = Money.of(currentAccountMoney.getCurrencyUnit(), transactionAmount);
        Money newAccountMoney = currentAccountMoney.minus(transactionMoney);
        account.setMoney(newAccountMoney);
        account.setAmount(newAccountMoney.getAmount());
        this.save(account);
    }

    @Override
    public List<Account> getAll() {
        return repository.findAll();
    }

    @Override
    public List<Account> getByNameStartsWithIgnoreCase(String name) {
        return repository.findByNameStartsWithIgnoreCase(name);
    }

    @Override
    public Account getById(long accountId) {
        Optional<Account> optionalAccount = repository.findById(accountId);
        return optionalAccount.orElseThrow(NoSuchAccountIdException::new);
    }

    @Override
    public List<Account> getAllByNeedAccountingIsTrue() {
        return repository.getAllByNeedAccountingIsTrue();
    }

    @Override
    public List<Account> getAllByNeedAccountingIsFalse() {
        return repository.getAllByNeedAccountingIsFalse();
    }
}
