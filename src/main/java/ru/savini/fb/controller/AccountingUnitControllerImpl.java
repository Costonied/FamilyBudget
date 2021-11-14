package ru.savini.fb.controller;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.stream.Collectors;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import ru.savini.fb.domain.entity.Category;
import ru.savini.fb.repo.AccountingUnitRepo;
import ru.savini.fb.domain.enums.CategoryCode;
import ru.savini.fb.domain.entity.AccountingUnit;
import ru.savini.fb.exceptions.NoSuchAccountingUnitIdException;

@Component
public class AccountingUnitControllerImpl implements AccountingUnitController {
    private final AccountingUnitRepo accountingUnitRepo;
    private final CategoryController categoryController;

    @Autowired
    public AccountingUnitControllerImpl(AccountingUnitRepo accountingUnitRepo,
                                        CategoryController categoryController) {
        this.accountingUnitRepo = accountingUnitRepo;
        this.categoryController = categoryController;
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
    public List<AccountingUnit> getAllByYearAndMonth(int year, int month) {
        return accountingUnitRepo.findAllByYearAndMonth(year, month);
    }

    @Override
    public AccountingUnit getById(long accountingUnitId) {
        Optional<AccountingUnit> optionalAccountingUnit = accountingUnitRepo.findById(accountingUnitId);
        return optionalAccountingUnit.orElseThrow(NoSuchAccountingUnitIdException::new);
    }

    @Override
    public AccountingUnit getByCategoryAndLocalDate(Category category, LocalDate localDate) {
        int year = localDate.getYear();
        int month = localDate.getMonthValue();
        AccountingUnit accountingUnit = accountingUnitRepo.findByCategoryAndYearAndMonth(category, year, month);
        if (accountingUnit == null) {
            accountingUnit = getNewEmptyAccountingUnit(category, year, month);
            save(accountingUnit);
        }
        return accountingUnit;
    }

    @Override
    public void increaseFactAmount(AccountingUnit accountingUnit, Money money) {
        Money currentFactMoney = getFactMoneyFromAccountingUnitAndMoney(accountingUnit, money);
        Money newFactMoney = currentFactMoney.plus(money);
        accountingUnit.setFactAmount(newFactMoney.getAmount());
        save(accountingUnit);
    }

    @Override
    public void decreaseFactAmount(AccountingUnit accountingUnit, Money money) {
        Money currentFactMoney = getFactMoneyFromAccountingUnitAndMoney(accountingUnit, money);
        Money newFactMoney = currentFactMoney.minus(money);
        accountingUnit.setFactAmount(newFactMoney.getAmount());
        save(accountingUnit);
    }

    @Override
    public List<Category> getAllCategory() {
        return categoryController.getAllForAccounting();
    }

    @Override
    public BigDecimal getAvailablePlanFunds(int year, int month) {
        List<AccountingUnit> accountingUnits = accountingUnitRepo.findAllByYearAndMonth(year, month);
        List<AccountingUnit> incomes = accountingUnits.stream()
                .filter(accountingUnit -> CategoryCode.isIncomeCategory(accountingUnit.getCategory()))
                .collect(Collectors.toList());
        List<AccountingUnit> spends = accountingUnits.stream()
                .filter(accountingUnit -> {
                    Category category = accountingUnit.getCategory();
                    return CategoryCode.isOutgoingCategory(category) || CategoryCode.isGoalsCategory(category);
                }).collect(Collectors.toList());
        BigDecimal incomesPlanFunds = calculatePlanAmount(incomes);
        BigDecimal spendsPlanFunds = calculatePlanAmount(spends);
        return incomesPlanFunds.subtract(spendsPlanFunds);
    }

    private Money getFactMoneyFromAccountingUnitAndMoney(AccountingUnit accountingUnit, Money transactionMoney) {
        CurrencyUnit currencyUnit = transactionMoney.getCurrencyUnit();
        return Money.of(currencyUnit, accountingUnit.getFactAmount());
    }

    private AccountingUnit getNewEmptyAccountingUnit(Category category, int year, int month) {
        AccountingUnit newAccountingUnit = new AccountingUnit();
        newAccountingUnit.setYear(year);
        newAccountingUnit.setMonth(month);
        newAccountingUnit.setCategory(category);
        return newAccountingUnit;
    }

    private BigDecimal calculatePlanAmount(List<AccountingUnit> accountingUnits) {
        BigDecimal total = BigDecimal.ZERO;
        for (AccountingUnit accountingUnit : accountingUnits) {
            total = total.add(accountingUnit.getPlanAmount());
        }
        return total;
    }
}
