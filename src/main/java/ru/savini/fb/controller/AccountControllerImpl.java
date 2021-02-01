package ru.savini.fb.controller;

import java.util.List;
import java.util.Optional;

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
    public void putMoney(double moneyAmount, Account account) {
        double currentAmount = account.getAmount();
        double newAmount = currentAmount + moneyAmount;
        account.setAmount(newAmount);
        this.save(account);
    }

    @Override
    public void withdrawMoney(double moneyAmount, Account account) {
        double currentAmount = account.getAmount();
        double newAmount = currentAmount - moneyAmount;
        account.setAmount(newAmount);
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
}
