package ru.savini.fb.domain.entity;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class AccountTest {
    private Account account;

    @BeforeClass
    public void setUp() {
        account = new Account();
    }

    @Test
    public void accountPropertyNeedAccountingIsFalseByDefault() {
        assertFalse(account.isNeedAccounting());
    }
}