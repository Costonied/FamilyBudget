package ru.savini.fb.domain.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Transaction {
    @Id
    @GeneratedValue(generator="TRANSACTION_GENERATOR")
    private Long id;
    private int categoryId;
    private Long accountId;
    private LocalDate date;
    private double amount;
    private String comment;

    public Transaction() {
        this.amount = 0.0;
        this.comment = "";
        this.accountId = 0L;
        this.categoryId = 0;
        this.date = LocalDate.now();
    }
}
