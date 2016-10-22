package space.wsq.kit.devkit.cache;

@SuppressWarnings({"unused"})
public interface ICache<K, V> {
    V put(K key, V value);

    void get(K key, ICacheRead<K, V> cacheRead);

    interface ICacheRead<K1, V1> {
        void onCache(K1 key, V1 value);
    }
}
