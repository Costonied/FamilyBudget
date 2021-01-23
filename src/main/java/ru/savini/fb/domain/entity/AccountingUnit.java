package ru.savini.fb.domain.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@ToString
public class AccountingUnit {
    @Id
    @GeneratedValue(generator="ACCOUNT_UNIT_GENERATOR")
    private Long id;
    private int year;
    private int month;
    private int categoryId;
    private double planAmount;
    private double factAmount;

    public AccountingUnit() {
        this.year = LocalDate.now().getYear();
        this.month = LocalDate.now().getMonthValue();
        this.categoryId = 0;
        this.planAmount = 0.0;
        this.factAmount = 0.0;
    }

    public AccountingUnit(Long id, int year, int month, int categoryId, double planAmount, double factAmount) {
        this.id = id;
        this.year = year;
        this.month = month;
        this.categoryId = categoryId;
        this.planAmount = planAmount;
        this.factAmount = factAmount;
    }
}
