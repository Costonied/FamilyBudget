package ru.savini.fb.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.savini.fb.domain.entity.TransactionPair;

public interface TransactionPairRepo extends JpaRepository<TransactionPair, Long> {

}
