package ru.savini.fb.controller;

import java.util.List;
import java.time.LocalDate;
import java.math.BigDecimal;

import org.joda.money.Money;
import ru.savini.fb.domain.entity.Category;
import ru.savini.fb.domain.entity.AccountingUnit;

public interface AccountingUnitController {
    void save(AccountingUnit accountingUnit);
    void delete(AccountingUnit accountingUnit);

    List<AccountingUnit> getAll();
    List<AccountingUnit> getAllByYearAndMonth(int year, int month);
    AccountingUnit getById(long accountingUnitId);
    AccountingUnit getByCategoryAndLocalDate(Category category, LocalDate localDate);

    void increaseFactAmount(AccountingUnit accountingUnit, Money money);
    void decreaseFactAmount(AccountingUnit accountingUnit, Money money);

    List<Category> getAllCategory();
    BigDecimal getAvailablePlanFunds(int year, int month);
}
