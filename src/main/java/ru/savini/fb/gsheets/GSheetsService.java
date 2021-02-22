package ru.savini.fb.gsheets;

import ru.savini.fb.domain.entity.Transaction;

import java.io.IOException;

public interface GSheetsService {
    void addTransaction(Transaction transaction) throws IOException;
}
