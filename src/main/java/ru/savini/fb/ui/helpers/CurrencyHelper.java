package ru.savini.fb.ui.helpers;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

public class CurrencyHelper {
    public static DecimalFormat format;

    private static List<String> currencyCode = new ArrayList<>();
    private static DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
    static {
        currencyCode.add("RUB");
        currencyCode.add("USD");
        currencyCode.add("EUR");

        decimalFormatSymbols.setDecimalSeparator('.');
        decimalFormatSymbols.setGroupingSeparator(' ');
        format = new DecimalFormat("###,###,##0.00", decimalFormatSymbols);
    }

    private CurrencyHelper() {
        // nothing
    }

    public static List<String> getCurrencyCode() {
        return currencyCode;
    }
}
