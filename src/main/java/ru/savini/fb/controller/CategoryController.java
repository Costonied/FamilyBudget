package ru.savini.fb.controller;

import ru.savini.fb.domain.entity.Category;

import java.util.List;

public interface CategoryController {
    void save(Category category);
    void delete(Category category);

    Category getById(int categoryId);
    List<Category> getAll();
    List<Category> getAllForAccounting();
    List<Category> getByNameStartsWithIgnoreCase(String name);

    List<String> getCategoryCodes();

    boolean isRepoEmpty();
    boolean hasWithdrawalCategory();
    boolean hasTransferCategory();
}
