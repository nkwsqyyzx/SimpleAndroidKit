package space.wsq.kit.devkit.cache;

import android.content.Context;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import space.wsq.kit.devkit.DeviceUtil;
import space.wsq.kit.devkit.Hash;
import space.wsq.kit.devkit.ILogger;

@SuppressWarnings({"unused"})
public class DiskCacheFacade {
    private static final String TAG = "DiskCacheFacade";
    private final DiskLruCache cache;

    public DiskCacheFacade(Context context) {
        int version = DeviceUtil.getAppVersion(context);
        DiskLruCache cache1;
        try {
            cache1 = DiskLruCache.open(context.getCacheDir(), version, 1, 100 * 1024 * 1024);
        } catch (IOException e) {
            cache1 = null;
            ILogger.defaultLogger.e(TAG, "failed to create cache", e);
        }
        cache = cache1;
    }

    public InputStream get(String key) {
        if (cache == null) {
            return null;
        }
        String k = Hash.md5(key);
        try {
            DiskLruCache.Snapshot sp = cache.get(k);
            if (sp == null) {
                return null;
            }
            return sp.getInputStream(0);
        } catch (IOException e) {
            ILogger.defaultLogger.e(TAG, "failed to get cache for key " + key, e);
            return null;
        }
    }

    public void put(String key, InputStream is) {
        if (cache == null) {
            return;
        }
        String k = Hash.md5(key);
        DiskLruCache.Editor editor;
        try {
            editor = cache.edit(k);
        } catch (IOException e) {
            ILogger.defaultLogger.e(TAG, "failed to write to stream for key " + key, e);
            return;
        }
        if (editor == null) {
            ILogger.defaultLogger.e(TAG, "got null editor for key " + key);
            return;
        }
        try {
            OutputStream os = editor.newOutputStream(0);
            BufferedInputStream bIS = new BufferedInputStream(is);
            byte[] data = new byte[1024];
            int len;
            while ((len = bIS.read(data)) != -1) {
                os.write(data, 0, len);
            }
            editor.commit();
        } catch (IOException e) {
            ILogger.defaultLogger.e(TAG, "failed to write to stream for key " + key, e);
            try {
                editor.abort();
            } catch (IOException e1) {
                ILogger.defaultLogger.e(TAG, "abort editor commit failed " + key, e1);
            }
        }
    }
}
