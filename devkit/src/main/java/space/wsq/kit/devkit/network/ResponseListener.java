package space.wsq.kit.devkit.network;

public interface ResponseListener<T> {
    void onResponse(T result);
    void onErrorResponse(Exception error);
}
