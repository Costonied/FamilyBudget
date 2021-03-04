package ru.savini.fb.domain.enums;

import ru.savini.fb.domain.entity.Category;

public enum CategoryCode {
    INCOME("INCOME"), OUTGO("OUTGO"), GOALS("GOALS");

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
}
