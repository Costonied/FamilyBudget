package ru.savini.fb.controller;

import ru.savini.fb.domain.entity.AccountingUnit;

import java.util.List;

public interface AccountingUnitController {
    void save(AccountingUnit accountingUnit);
    void delete(AccountingUnit accountingUnit);

    List<AccountingUnit> getAll();
    AccountingUnit getById(long accountingUnitId);
}
