package ru.savini.fb.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.savini.fb.domain.entity.Category;
import ru.savini.fb.domain.entity.AccountingUnit;

public interface AccountingUnitRepo extends JpaRepository<AccountingUnit, Long> {
    AccountingUnit findByCategoryAndMonth(Category category, int month);
}
