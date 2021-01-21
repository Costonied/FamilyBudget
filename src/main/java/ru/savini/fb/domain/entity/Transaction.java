package ru.savini.fb.domain.entity;

import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import java.time.LocalDate;

@Entity
public class Transaction {
    @Id
    @GeneratedValue(generator="TRANSACTION_GENERATOR")
    private Long id;
    private Long categoryId;
    private Long accountId;
    private LocalDate date;
    private double amount;
    private String comment;

    public Transaction() {
        this.amount = 0.0;
        this.comment = "";
        this.accountId = 0L;
        this.categoryId = 0L;
        this.date = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getAmount() {
        return amount;
    }

    public String getComment() {
        return comment;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
