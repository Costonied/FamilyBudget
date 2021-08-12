package ru.savini.fb.domain.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Transaction {
    @Id
    @GeneratedValue(generator="TRANSACTION_GENERATOR")
    private Long id;
    private LocalDate date;
    private BigDecimal amount;
    private String type;
    private String comment;
    @ManyToOne private Category category;
    @ManyToOne private Account account;

    public Transaction() {
        this.amount = BigDecimal.ZERO;
        this.comment = "";
        this.date = LocalDate.now();
    }

    public Transaction(Transaction fromTransaction, Account account, String type) {
        this.id = fromTransaction.getId();
        this.date = fromTransaction.getDate();
        this.amount = fromTransaction.getAmount();
        this.comment = fromTransaction.getComment();
        this.category = fromTransaction.getCategory();
        this.account = account;
        this.type = type;
    }
}
