package space.wsq.kit.devkit.cache;

import android.graphics.Bitmap;

@SuppressWarnings({"unused"})
public class BitmapCache implements ICache<String, Bitmap> {
    private final SimpleCache<String, Bitmap> memory;

    public BitmapCache(int maxSize) {
        memory = new SimpleCache<String, Bitmap>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.isRecycled() ? 0 : value.getHeight() * value.getRowBytes();
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                // don't recycle here.
            }
        };
    }

    @Override
    public Bitmap put(String key, Bitmap value) {
        return memory.put(key, value);
    }

    @Override
    public void get(String key, ICacheRead<String, Bitmap> cacheRead) {
        Bitmap bitmap = memory.get(key);
        // invalid cache when bitmap is recycled.
        if (bitmap != null && bitmap.isRecycled()) {
            bitmap = null;
        }
        cacheRead.onCache(key, bitmap);
    }
}
