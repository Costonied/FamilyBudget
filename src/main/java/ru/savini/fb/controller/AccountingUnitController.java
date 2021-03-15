package ru.savini.fb.controller;

import java.time.LocalDate;
import java.util.List;

import ru.savini.fb.domain.entity.Category;
import ru.savini.fb.domain.entity.AccountingUnit;

public interface AccountingUnitController {
    void save(AccountingUnit accountingUnit);
    void delete(AccountingUnit accountingUnit);

    List<AccountingUnit> getAll();
    AccountingUnit getById(long accountingUnitId);
    AccountingUnit getByCategoryAndLocalDate(Category category, LocalDate localDate);

    void increaseFactAmount(AccountingUnit accountingUnit, double amount);
    void decreaseFactAmount(AccountingUnit accountingUnit, double amount);
}
