package ru.savini.fb.gsheets;

public class GSheetsUtils {
    public static double getDoubleFromString(String stringWithDouble) {
        String stringDoubleAfterReplace = stringWithDouble.replaceFirst(",", ".");
        return Double.parseDouble(stringDoubleAfterReplace);
    }
}
