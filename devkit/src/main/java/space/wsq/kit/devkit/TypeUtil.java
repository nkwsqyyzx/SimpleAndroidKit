package space.wsq.kit.devkit;

@SuppressWarnings("unused")
public class TypeUtil {
    public static int convertToInt(String value, int fallback) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    public static long convertToLong(String value, long fallback) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    public static float convertToFloat(String value, float fallback) {
        try {
            return Float.valueOf(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    public static double convertToDouble(String value, double fallback) {
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
