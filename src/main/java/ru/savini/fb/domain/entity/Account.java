package ru.savini.fb.domain.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class Account {

    @Id
    @GeneratedValue(generator="ACCOUNT_GENERATOR")
    private Long id;
    private double amount;
    private String name;
    private String currency;

    protected Account() {
    }

    public Account(String name, double amount, String currency) {
        this.name = name;
        this.amount = amount;
        this.currency = currency;
    }

    public Account(Long id, String name, double amount, String currency) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.currency = currency;
    }

    @Override
    public String toString() {
        return String.format("Account[id=%d, name='%s', amount='%.2f', currency='%s']",
                id, name, amount, currency);
    }

}