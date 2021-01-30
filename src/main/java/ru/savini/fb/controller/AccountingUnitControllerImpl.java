package ru.savini.fb.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import ru.savini.fb.exceptions.NoSuchAccountingUnitIdException;
import ru.savini.fb.repo.AccountingUnitRepo;
import ru.savini.fb.domain.entity.AccountingUnit;

@Component
public class AccountingUnitControllerImpl implements AccountingUnitController {
    private final AccountingUnitRepo accountingUnitRepo;

    @Autowired
    public AccountingUnitControllerImpl(AccountingUnitRepo accountingUnitRepo) {
        this.accountingUnitRepo = accountingUnitRepo;
    }

    @Override
    public void save(AccountingUnit accountingUnit) {
        accountingUnitRepo.save(accountingUnit);
    }

    @Override
    public void delete(AccountingUnit accountingUnit) {
        accountingUnitRepo.delete(accountingUnit);
    }

    @Override
    public List<AccountingUnit> getAll() {
        return accountingUnitRepo.findAll();
    }

    @Override
    public AccountingUnit getById(long accountingUnitId) {
        Optional<AccountingUnit> optionalAccountingUnit = accountingUnitRepo.findById(accountingUnitId);
        return optionalAccountingUnit.orElseThrow(NoSuchAccountingUnitIdException::new);
    }
}
