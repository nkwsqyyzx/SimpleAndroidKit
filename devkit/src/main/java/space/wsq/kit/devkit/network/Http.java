package space.wsq.kit.devkit.network;

import android.content.Context;

public final class Http extends BaseHttp {
    private final IHttp proxy;

    private Http(Context context) {
        proxy = new OriginHttp(context);
    }

    @Override
    public void execute(Request request, StreamListener listener) {
        proxy.execute(request, listener);
    }

    public static IHttp getInstance(Context context) {
        if (instance == null) {
            synchronized (IHttp.class) {
                if (instance == null) {
                    instance = new Http(context);
                }
            }
        }
        return instance;
    }

    private static IHttp instance;
}
