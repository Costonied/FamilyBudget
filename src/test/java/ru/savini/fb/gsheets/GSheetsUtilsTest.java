package ru.savini.fb.gsheets;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class GSheetsUtilsTest {

    @BeforeMethod
    public void setUp() {
    }

    @Test
    public void testGetDoubleFromString() {
        double expected = 1.23;
        String doubleString = "1,23";
        double result = GSheetsUtils.getDoubleFromString(doubleString);
        assertEquals(result, expected);
    }
}