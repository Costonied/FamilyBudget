package ru.savini.fb.controller;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import ru.savini.fb.repo.AccountingUnitRepo;
import ru.savini.fb.domain.entity.Category;
import ru.savini.fb.domain.entity.AccountingUnit;
import ru.savini.fb.exceptions.NoSuchAccountingUnitIdException;

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

    @Override
    public AccountingUnit getByCategoryAndLocalDate(Category category, LocalDate localDate) {
        int month = localDate.getMonthValue();
        return accountingUnitRepo.findByCategoryAndMonth(category, month);
    }

    @Override
    public void increaseFactAmount(AccountingUnit accountingUnit, double amount) {
        double currentFactAmount = accountingUnit.getFactAmount();
        double newFactAmount = currentFactAmount + amount;
        accountingUnit.setFactAmount(newFactAmount);
        save(accountingUnit);
    }
}
