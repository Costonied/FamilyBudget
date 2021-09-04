package ru.savini.fb.domain.entity;

import lombok.Getter;
import lombok.Setter;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class Account {

    @Id
    @GeneratedValue(generator="ACCOUNT_GENERATOR")
    private Long id;
    private BigDecimal amount;
    private String name;
    private String currency;
    @Column(columnDefinition = "boolean default false")
    private boolean needAccounting;
    @Transient private Money money;

    protected Account() {
    }

    public Account(String name, BigDecimal amount, String currency) {
        this.name = name;
        this.currency = currency;
        setMoney(currency, amount);
    }

    public Account(Long id, String name, BigDecimal amount, String currency) {
        this.id = id;
        this.name = name;
        this.currency = currency;
        setMoney(currency, amount);
    }

    public Money getMoney() {
        if (this.money == null) {
            this.setMoney(this.currency, this.amount);
        }
        return this.money;
    }

    public void setMoney(String currency, BigDecimal amount) {
        this.money = Money.of(CurrencyUnit.of(currency), amount);
    }

    @Override
    public String toString() {
        return String.format("Account[id=%d, name='%s', amount='%s', currency='%s']",
                id, name, amount, currency);
    }

}