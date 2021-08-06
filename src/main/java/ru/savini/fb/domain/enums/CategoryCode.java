package ru.savini.fb.domain.enums;

import ru.savini.fb.domain.entity.Category;

public enum CategoryCode {
    INCOME("INCOME"), OUTGO("OUTGO"), GOALS("GOALS"), TRANSFER("TRANSFER");

    private String code;

    CategoryCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static boolean isGoalsCategory(Category category) {
        return category.getType().equals(CategoryCode.GOALS.getCode());
    }

    public static boolean isTransferCategory(Category category) {
        return category.getType().equals(CategoryCode.TRANSFER.getCode());
    }

    public static boolean isIncomeCategory(Category category) {
        return category.getType().equals(CategoryCode.INCOME.getCode());
    }

    public static boolean isOutgoingCategory(Category category) {
        return category.getType().equals(CategoryCode.OUTGO.getCode());
    }
}
