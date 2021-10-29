package ru.savini.fb.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.savini.fb.domain.entity.Category;

import java.util.List;

public interface CategoryRepo extends JpaRepository<Category, Integer> {
    int countByTypeEquals(String categoryType);
    List<Category> findAllByTypeIn(List<String> types);
    List<Category> findByNameStartsWithIgnoreCase(String name);
}
