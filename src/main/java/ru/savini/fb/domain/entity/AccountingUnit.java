package ru.savini.fb.domain.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.GeneratedValue;
import javax.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@ToString
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "year", "month", "category_id" }) })
public class AccountingUnit {
    @Id
    @GeneratedValue(generator="ACCOUNT_UNIT_GENERATOR")
    private Long id;
    private int year;
    private int month;
    private BigDecimal planAmount;
    private BigDecimal factAmount;
    @ManyToOne private Category category;

    public AccountingUnit() {
        this.year = LocalDate.now().getYear();
        this.month = LocalDate.now().getMonthValue();
        this.planAmount = BigDecimal.ZERO;
        this.factAmount = BigDecimal.ZERO;
    }
}
