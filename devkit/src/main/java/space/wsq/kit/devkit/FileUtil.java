package space.wsq.kit.devkit;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

@SuppressWarnings({"unused"})
public final class FileUtil {
    private static final String TAG = "FileUtil";

    public static void copyFromAssets(Context context, String pathInAssets, String destination) throws IOException {
        InputStream fis = context.getAssets().open(pathInAssets);
        try {
            saveStreamToFile(fis, new File(destination));
        } finally {
            SafeUtil.safeClose(fis);
        }
    }

    public static void saveStreamToFile(InputStream stream, File destination) throws IOException {
        FileOutputStream fos = new FileOutputStream(destination);
        ReadableByteChannel src = Channels.newChannel(stream);
        FileChannel dest = fos.getChannel();
        try {
            long count = 4 * 1024 * 1024;
            long start = 0;
            long copied = count;
            while (copied > 0) {
                copied = dest.transferFrom(src, start, count);
                start += copied;
            }
        } finally {
            SafeUtil.safeClose(fos);
        }
    }

    public static int deleteDir(String dir) {
        return deleteDir(new File(dir));
    }

    public static int deleteDir(File dir) {
        int count = 0;
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        count += file.delete() ? 1 : 0;
                    } else {
                        count += deleteDir(file);
                    }
                }
            }
            boolean ignore = dir.delete();
            if (!ignore) {
                ILogger.defaultLogger.e(TAG, "delete dir " + dir.getAbsolutePath() + " failed.");
            }
        }
        return count;
    }
}
