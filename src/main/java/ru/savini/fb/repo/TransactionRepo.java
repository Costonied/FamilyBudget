package ru.savini.fb.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.savini.fb.domain.entity.Transaction;

import java.util.List;

public interface TransactionRepo extends JpaRepository<Transaction, Long> {
    void deleteById(String id);
    List<Transaction> getAllByOrderByDateDesc();
    List<Transaction> findAllByAccount_NameContainsIgnoreCaseOrderByDateDesc(String accountName);
}
