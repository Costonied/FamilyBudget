package ru.savini.fb.domain.enums;

public enum CategoryCode {
    INCOME("INCOME"), OUTGO("OUTGO");

    private String code;

    CategoryCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
