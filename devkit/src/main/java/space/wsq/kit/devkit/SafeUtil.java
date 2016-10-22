package space.wsq.kit.devkit;

import java.io.Closeable;
import java.io.IOException;

@SuppressWarnings("unused")
public class SafeUtil {
    private static final String TAG = "SafeUtil";

    public static void safeClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ex) {
                ILogger.defaultLogger.e(TAG, "error closing " + closeable.getClass().getName(), ex);
            }
        }
    }
}
