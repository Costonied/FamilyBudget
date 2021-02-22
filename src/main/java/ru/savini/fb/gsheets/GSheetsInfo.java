package ru.savini.fb.gsheets;

/**
 * Information about FamilyBudget sheets
 */
public class GSheetsInfo {
    /* Common info about document */
    public static final String SPREADSHEET_ID = "1BB-MFczIhtcZdYPUS9htPEpMsu-vxCDaFcQ4qsjWqgY";
    /* Transactions info */
    public static final String TRANSACTIONS_RANGE = "Transactions!A2:G";
    public static final int TRANSACTION_ID_IDX = 0;
    public static final int TRANSACTION_CAT_ID_IDX = 1;
    public static final int TRANSACTION_CAT_NAME_IDX = 2;
    public static final int TRANSACTION_DATE_IDX = 3;
    public static final int TRANSACTION_AMOUNT_IDX = 4;
    public static final int TRANSACTION_ACC_ID_IDX = 5;
    public static final int TRANSACTION_ACC_NAME_IDX = 6;

    private GSheetsInfo() {
        // just hide constructor
    }
}
