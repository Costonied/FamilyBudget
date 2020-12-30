package ru.savini.fb.domain;

public enum Currency {
    RUB(Codes.RUB), USD(Codes.USD), EUR(Codes.EUR);

    private String code;

    Currency(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static class Codes {
        public static final String RUB = "RUB";
        public static final String USD = "US";
        public static final String EUR = "EUR";
    }
}
