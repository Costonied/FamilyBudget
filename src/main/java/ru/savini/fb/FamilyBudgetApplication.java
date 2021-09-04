package ru.savini.fb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import ru.savini.fb.domain.entity.Category;
import ru.savini.fb.domain.enums.CategoryCode;
import ru.savini.fb.controller.CategoryController;

@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
public class FamilyBudgetApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(FamilyBudgetApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(FamilyBudgetApplication.class);
    }

    @Bean
    public CommandLineRunner loadData(CategoryController categoryController) {
        return args -> initCategories(categoryController);
    }

    private void initCategories(CategoryController categoryController) {
        if (!categoryController.hasWithdrawalCategory()) {
            createCategory(categoryController, CategoryCode.WITHDRAWAL, "Снятие со счета");
        }
        if (!categoryController.hasTransferCategory()) {
            createCategory(categoryController, CategoryCode.TRANSFER, "Перевод между счетами");
        }
    }

    private void createCategory(CategoryController categoryController, CategoryCode categoryCode, String categoryName) {
        Category category = new Category(categoryName, categoryCode.getCode());
        categoryController.save(category);
        LOGGER.info("Category \"{}\" was created", categoryName);
    }
}
