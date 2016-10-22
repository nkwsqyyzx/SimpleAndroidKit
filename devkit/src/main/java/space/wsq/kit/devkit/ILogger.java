package space.wsq.kit.devkit;

@SuppressWarnings("unused")
public interface ILogger {
    ILogger defaultLogger = new AdbLogger();

    int v(String tag, String msg);

    int v(String tag, String msg, Throwable tr);

    int d(String tag, String msg);

    int d(String tag, String msg, Throwable tr);

    int i(String tag, String msg);

    int i(String tag, String msg, Throwable tr);

    int w(String tag, String msg);

    int w(String tag, String msg, Throwable tr);

    int w(String tag, Throwable tr);

    int e(String tag, String msg);

    int e(String tag, String msg, Throwable tr);

    int wtf(String tag, String msg);

    int wtf(String tag, Throwable tr);

    int wtf(String tag, String msg, Throwable tr);
}
