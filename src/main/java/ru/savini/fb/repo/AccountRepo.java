package ru.savini.fb.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.savini.fb.domain.entity.Account;

import java.util.List;

public interface AccountRepo extends JpaRepository<Account, Long> {
    List<Account> getAllByNeedAccountingIsTrue();
    List<Account> getAllByNeedAccountingIsFalse();
    List<Account> findByNameContainsIgnoreCase(String lastName);
}