package ru.savini.fb.domain.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.GeneratedValue;

@Entity
@Getter
@Setter
public class TransactionPair {
    @Id
    @GeneratedValue(generator="TRANSACTION_PAIR_GENERATOR")
    private Long id;
    @OneToOne private Transaction debitTransaction;
    @OneToOne private Transaction creditTransaction;

    public TransactionPair() {
        // nothing
    }

    public TransactionPair(Transaction debitTransaction, Transaction creditTransaction) {
        this.debitTransaction = debitTransaction;
        this.creditTransaction = creditTransaction;
    }
}
