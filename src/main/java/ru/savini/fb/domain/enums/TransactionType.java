package ru.savini.fb.domain.enums;

public enum TransactionType {
    DEBIT("D"), CREDIT("C");
    private String type;
    
    TransactionType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
