package ru.savini.fb.ui.models;

import ru.savini.fb.domain.entity.AccountingUnit;
import ru.savini.fb.domain.entity.Category;

import java.math.BigDecimal;

public class AccountingCategory {
    private Category category;
    private AccountingUnit accountingUnit;

    public AccountingCategory(Category category, AccountingUnit accountingUnit) {
        this.category = category;
        this.accountingUnit = accountingUnit;
    }

    public Category getCategory() {
        return category;
    }

    public AccountingUnit getAccountingUnit() {
        return accountingUnit;
    }

    public String getCategoryName() {
        return category.getName();
    }

    public BigDecimal getFactAmount() {
        if (accountingUnit == null) {
            return BigDecimal.ZERO;
        } else {
            return accountingUnit.getFactAmount();
        }
    }

    public BigDecimal getPlanAmount() {
        if (accountingUnit == null) {
            return BigDecimal.ZERO;
        } else {
            return accountingUnit.getPlanAmount();
        }
    }
}
