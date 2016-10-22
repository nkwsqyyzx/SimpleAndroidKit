package space.wsq.kit.devkit.cache;

import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings({"unused"})
public class SimpleCache<K, V> implements ICache<K, V> {
    private final LinkedHashMap<K, V> map;

    private int size;
    private int maxSize;

    private int putCount;
    private int evictionCount;
    private int hitCount;
    private int missCount;

    public SimpleCache(int maxSize) {
        this.maxSize = maxSize;
        this.map = new LinkedHashMap<>(0, 0.75f, true);
    }

    public final V get(K key) {
        if (key == null) {
            return null;
        }

        V mapValue;
        synchronized (this) {
            mapValue = map.get(key);
            if (mapValue != null) {
                hitCount++;
                return mapValue;
            }
            missCount++;
        }
        return null;
    }

    @Override
    public void get(K key, ICacheRead<K, V> cacheRead) {
        cacheRead.onCache(key, get(key));
    }

    public final V put(K key, V value) {
        if (key == null || value == null) {
            return null;
        }

        V previous;
        synchronized (this) {
            putCount++;
            size += sizeOf(key, value);
            previous = map.put(key, value);
            if (previous != null) {
                size -= sizeOf(key, previous);
            }
        }

        if (previous != null) {
            entryRemoved(false, key, previous, value);
        }

        trimToSize(maxSize);
        return previous;
    }

    public synchronized boolean freeMemory(int sizeToFree) {
        int newSize = size + sizeToFree;
        if (newSize > maxSize * 0.8) {
            trimToSize(size - sizeToFree);
        }
        return true;
    }

    public void trimToSize(int maxSize) {
        while (true) {
            K key;
            V value;
            synchronized (this) {
                if (size < 0 || (map.isEmpty() && size != 0)) {
                    throw new IllegalStateException(getClass().getName()
                            + ".sizeOf() is reporting inconsistent results!");
                }

                if (size <= maxSize || map.isEmpty()) {
                    break;
                }

                Map.Entry<K, V> toEvict = map.entrySet().iterator().next();
                key = toEvict.getKey();
                value = toEvict.getValue();
                map.remove(key);
                size -= sizeOf(key, value);
                evictionCount++;
            }

            entryRemoved(true, key, value, null);
        }
    }

    public final V remove(K key) {
        if (key == null) {
            return null;
        }

        V previous;
        synchronized (this) {
            previous = map.remove(key);
            if (previous != null) {
                size -= sizeOf(key, previous);
            }
        }

        if (previous != null) {
            entryRemoved(false, key, previous, null);
        }

        return previous;
    }

    protected void entryRemoved(boolean evicted, K key, V oldValue, V newValue) {
    }

    protected int sizeOf(K key, V value) {
        return 1;
    }

    public final void evictAll() {
        trimToSize(-1); // -1 will evict 0-sized elements
    }

    public synchronized final void clear() {
        evictAll();
        putCount = 0;
        evictionCount = 0;
        hitCount = 0;
        missCount = 0;
    }

    public final int size() {
        return size;
    }

    public final int getMaxSize() {
        return maxSize;
    }

    public final int hitCount() {
        return hitCount;
    }

    public final int missCount() {
        return missCount;
    }

    public final int putCount() {
        return putCount;
    }

    public final int evictionCount() {
        return evictionCount;
    }
}
