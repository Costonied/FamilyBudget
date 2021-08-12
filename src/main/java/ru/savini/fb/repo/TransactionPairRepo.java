package ru.savini.fb.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.savini.fb.domain.entity.Transaction;
import ru.savini.fb.domain.entity.TransactionPair;

public interface TransactionPairRepo extends JpaRepository<TransactionPair, Long> {
    TransactionPair getTransactionPairByCreditTransaction(Transaction creditTransaction);
    TransactionPair getTransactionPairByDebitTransaction(Transaction debitTransaction);
}
