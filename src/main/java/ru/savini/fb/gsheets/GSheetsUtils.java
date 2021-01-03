package ru.savini.fb.gsheets;

public class GSheetsUtils {

    private GSheetsUtils() {
        // hide constructor
    }

    public static long getLongFromObj(Object object) {
        return Long.parseLong(object.toString());
    }

    public static String getStringFromObj(Object object) {
        return object.toString();
    }

    public static double getDoubleFromObj(Object object) {
        String stringDoubleAfterReplace = object.toString()
                .replaceFirst(",", ".");
        return Double.parseDouble(stringDoubleAfterReplace);
    }
}
