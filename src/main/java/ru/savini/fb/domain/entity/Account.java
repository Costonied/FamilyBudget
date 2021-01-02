package ru.savini.fb.domain.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Account {

    @Id
    @GeneratedValue
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

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        return String.format("Account[id=%d, name='%s', amount='%.2f', currency='%s']",
                id, name, amount, currency);
    }

}