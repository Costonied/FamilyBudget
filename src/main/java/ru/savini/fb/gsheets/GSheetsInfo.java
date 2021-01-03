package ru.savini.fb.gsheets;

/**
 * Information about FamilyBudget sheets
 */
public class GSheetsInfo {
    /* Common info about document */
    public static final String SPREADSHEET_ID = "1BB-MFczIhtcZdYPUS9htPEpMsu-vxCDaFcQ4qsjWqgY";
    /* Accounts info */
    public static final String ACCOUNTS_RANGE = "Accounts!A2:D";
    public static final int ACCOUNT_ID_IDX = 0;
    public static final int ACCOUNT_NAME_IDX = 1;
    public static final int ACCOUNT_AMOUNT_IDX = 2;
    public static final int ACCOUNT_CURRENCY_IDX = 3;

    private GSheetsInfo() {
        // just hide constructor
    }
}
