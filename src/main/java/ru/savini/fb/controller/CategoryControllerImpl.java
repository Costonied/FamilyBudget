package ru.savini.fb.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import ru.savini.fb.repo.CategoryRepo;
import ru.savini.fb.domain.entity.Category;
import ru.savini.fb.domain.enums.CategoryCode;
import ru.savini.fb.exceptions.NoSuchCategoryIdException;

@Component
public class CategoryControllerImpl implements CategoryController {
    private final CategoryRepo categoryRepo;

    @Autowired
    public CategoryControllerImpl(CategoryRepo categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    @Override
    public void save(Category category) {
        categoryRepo.save(category);
    }

    @Override
    public void delete(Category category) {
        categoryRepo.delete(category);
    }

    @Override
    public Category getById(int categoryId) {
        Optional<Category> optionalCategory = categoryRepo.findById(categoryId);
        return optionalCategory.orElseThrow(NoSuchCategoryIdException::new);
    }

    @Override
    public List<Category> getAll() {
        return categoryRepo.findAll();
    }

    @Override
    public List<Category> getAllForAccounting() {
        List<String> types = new ArrayList<>();
        types.add(CategoryCode.INCOME.getCode());
        types.add(CategoryCode.OUTGO.getCode());
        types.add(CategoryCode.GOALS.getCode());
        return categoryRepo.findAllByTypeIn(types);
    }

    @Override
    public List<Category> getByNameStartsWithIgnoreCase(String name) {
        return categoryRepo.findByNameStartsWithIgnoreCase(name);
    }

    @Override
    public List<String> getCategoryCodes() {
        List<CategoryCode> enumCodes = Arrays.asList(CategoryCode.values());
        return enumCodes.stream()
                .map(CategoryCode::getCode)
                .filter(categoryCode -> !categoryCode.equalsIgnoreCase(CategoryCode.TRANSFER.getCode()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isRepoEmpty() {
        return this.getAll().isEmpty();
    }

    @Override
    public boolean hasWithdrawalCategory() {
        return categoryRepo.countByTypeEquals(CategoryCode.WITHDRAWAL.getCode()) > 0;
    }

    @Override
    public boolean hasTransferCategory() {
        return categoryRepo.countByTypeEquals(CategoryCode.TRANSFER.getCode()) > 0;
    }
}
