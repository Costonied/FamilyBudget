package ru.savini.fb.domain.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
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
    private double planAmount;
    private double factAmount;
    @ManyToOne private Category category;

    public AccountingUnit() {
        this.year = LocalDate.now().getYear();
        this.month = LocalDate.now().getMonthValue();
        this.planAmount = 0.0;
        this.factAmount = 0.0;
    }
}
