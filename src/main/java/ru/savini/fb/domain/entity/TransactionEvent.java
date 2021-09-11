package ru.savini.fb.domain.entity;

import lombok.Getter;
import lombok.Setter;
import ru.savini.fb.domain.enums.TransactionType;

import java.time.LocalDate;
import java.math.BigDecimal;

/**
 * Transaction event using for process of transaction Creation and Edition
 */
@Getter
@Setter
public class TransactionEvent {
    private Long id;
    private String comment;
    private LocalDate date;
    private BigDecimal amount;
    private Category category;
    private Account debitAccount;
    private Account creditAccount;

    public TransactionEvent() {
        this.comment = "";
        this.date = LocalDate.now();
        this.amount = BigDecimal.ZERO;
    }

    public TransactionEvent(LocalDate date) {
        this();
        this.date = date;
    }

    public TransactionEvent(Transaction transaction) {
        this.id = transaction.getId();
        this.comment = transaction.getComment();
        this.date = transaction.getDate();
        this.amount = transaction.getAmount();
        this.category = transaction.getCategory();
        if (TransactionType.CREDIT.getType().equals(transaction.getType())) {
            this.creditAccount = transaction.getAccount();
        } else {
            this.debitAccount = transaction.getAccount();
        }
    }
}
