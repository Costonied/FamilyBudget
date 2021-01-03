package ru.savini.fb.ui.helpers;

import java.util.ArrayList;
import java.util.List;

public class CategoryHelper {
    private static List<String> categoryCode = new ArrayList<>();
    static {
        categoryCode.add("INCOME");
        categoryCode.add("OUTGO");
    }

    public static List<String> getCategoryCode() {
        return categoryCode;
    }
}
