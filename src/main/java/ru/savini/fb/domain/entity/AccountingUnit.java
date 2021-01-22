package ru.savini.fb.domain.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AccountingUnit {
    @Id
    @GeneratedValue(generator="ACCOUNT_UNIT_GENERATOR")
    private Long id;
    private int year;
    private int month;
    private int categoryId;
    private double amount;

    public AccountingUnit(Long id, int year, int month, int categoryId, double amount) {
        this.id = id;
        this.year = year;
        this.month = month;
        this.categoryId = categoryId;
        this.amount = amount;
    }
}