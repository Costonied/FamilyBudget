package ru.savini.fb.ui.helpers;

import java.util.ArrayList;
import java.util.List;

public class AccountHelper {
    private static List<String> currencyCode = new ArrayList<>();
    static {
        currencyCode.add("RUB");
        currencyCode.add("USD");
        currencyCode.add("EUR");
    }

    public static List<String> getCurrencyCode() {
        return currencyCode;
    }
}
