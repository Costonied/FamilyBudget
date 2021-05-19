package ru.savini.fb.controller;

import java.time.LocalDate;
import java.util.List;

import org.joda.money.Money;
import ru.savini.fb.domain.entity.Category;
import ru.savini.fb.domain.entity.AccountingUnit;

public interface AccountingUnitController {
    void save(AccountingUnit accountingUnit);
    void delete(AccountingUnit accountingUnit);

    List<AccountingUnit> getAll();
    AccountingUnit getById(long accountingUnitId);
    AccountingUnit getByCategoryAndLocalDate(Category category, LocalDate localDate);

    void increaseFactAmount(AccountingUnit accountingUnit, Money money);
    void decreaseFactAmount(AccountingUnit accountingUnit, Money money);
}
