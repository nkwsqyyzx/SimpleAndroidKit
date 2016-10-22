package space.wsq.kit.devkit.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import space.wsq.kit.devkit.BitmapUtil;
import space.wsq.kit.devkit.ILogger;
import space.wsq.kit.devkit.SafeUtil;

@SuppressWarnings({"unused"})
public final class Request {
    private static final String TAG = "Request";

    private final IHttp http;
    private final Handler handler;
    private final RequestError error;
    protected final URL url;
    protected final String method;
    protected final Map<String, String> headers;
    protected final Map<String, String> body;
    protected final Object tag;

    protected boolean canUseCache;

    private Request(Builder builder) {
        this.error = builder.error;
        Context context = builder.context;
        this.handler = new Handler(context.getMainLooper());
        this.url = builder.url;
        this.method = builder.method;
        this.headers = builder.headers;
        this.body = builder.body;
        this.canUseCache = builder.canUseCache;
        this.tag = builder.tag != null ? builder.tag : this;
        this.http = Http.getInstance(context);
    }

    private void runOnMainThread(Runnable runnable) {
        handler.post(runnable);
    }

    public void execute(final StringListener listener) {
        if (error != null) {
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    listener.onErrorResponse(error);
                }
            });
            return;
        }
        http.execute(this, new StreamListener() {

            @Override
            public void onResponse(InputStream result) {
                byte[] data = toBytes(result);
                final String value = new String(data);
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onResponse(value);
                    }
                });
            }

            @Override
            public void onErrorResponse(final Exception error) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onErrorResponse(error);
                    }
                });
            }
        });
    }

    public void execute(final int maxWidth, final int maxHeight, final ImageView.ScaleType scaleType, final Bitmap.Config decodeConfig, final BitmapListener listener) {
        if (error != null) {
            listener.onErrorResponse(error);
            return;
        }
        this.canUseCache = true;
        http.execute(this, new StreamListener() {
            @Override
            public void onResponse(InputStream result) {
                byte[] data = toBytes(result);
                try {
                    final Bitmap bitmap = BitmapUtil.decodeFromByteArray(data, maxWidth, maxHeight, scaleType, decodeConfig);
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onResponse(bitmap);
                        }
                    });
                } catch (final Throwable ex) {
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onErrorResponse(new Exception(ex));
                        }
                    });
                }
            }

            @Override
            public void onErrorResponse(final Exception error) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onErrorResponse(error);
                    }
                });
            }
        });
    }

    public void execute(final StreamListener listener) {
        http.execute(this, listener);
    }

    private static byte[] toBytes(InputStream stream) {
        if (stream == null) {
            return new byte[0];
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            byte[] data = new byte[1024];
            try {
                int len;
                while ((len = stream.read(data)) != -1) {
                    os.write(data, 0, len);
                }
            } catch (IOException e) {
                ILogger.defaultLogger.e(TAG, "failed to read is", e);
            }
            return os.toByteArray();
        } finally {
            SafeUtil.safeClose(os);
        }
    }

    protected boolean hasBody() {
        return body != null && body.size() > 0;
    }

    public boolean shouldSaveCache() {
        return this.canUseCache();
    }

    public boolean canUseCache() {
        return this.canUseCache;
    }

    public static class Builder {
        private final Context context;
        private final String rawUrl;
        private URL url;
        private String method;
        private Map<String, String> headers;
        private Map<String, String> body;
        private boolean canUseCache;
        private Object tag;

        private RequestError error;

        public Builder(Context context, String url) {
            this.context = context.getApplicationContext();
            this.rawUrl = url;
        }

        public Request build() {
            if (method == null) {
                // default method is GET
                get();
            }
            if ("GET".equals(method)) {
                try {
                    this.url = BaseHttp.appendParameters(this.url.toURI(), body).toURL();
                } catch (URISyntaxException e) {
                    this.error = new RequestError("Failed to add parameters to url", e);
                } catch (MalformedURLException e) {
                    this.error = new RequestError("Failed to create url", e);
                }
            }
            return new Request(this);
        }

        public Builder get() {
            if (this.url != null) {
                this.error = new RequestError("Method called twice");
            }
            try {
                this.url = new URL(rawUrl);
            } catch (MalformedURLException e) {
                this.error = new RequestError(e);
            }
            this.method = "GET";
            return this;
        }

        public Builder post() {
            if (this.url != null) {
                this.error = new RequestError("Method called twice");
            }
            try {
                this.url = new URL(rawUrl);
            } catch (MalformedURLException e) {
                this.error = new RequestError(e);
            }
            this.method = "POST";
            return this;
        }

        public Builder canUseCache(boolean canUseCache) {
            this.canUseCache = canUseCache;
            return this;
        }

        public Builder addHeader(String name, String value) {
            if (headers == null) {
                headers = new HashMap<>();
            }
            headers.put(name, value.trim());
            return this;
        }

        public Builder addHeader(Map<String, String> headers) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                addHeader(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public Builder addBody(String key, String value) {
            if (body == null) {
                body = new HashMap<>();
            }
            body.put(key, value);
            return this;
        }

        public Builder addBody(Map<String, String> params) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                addBody(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public Builder tag(Object tag) {
            this.tag = tag;
            return this;
        }
    }

    public static class RequestError extends IOException {
        public RequestError() {
        }

        public RequestError(String detailMessage) {
            super(detailMessage);
        }

        public RequestError(String message, Throwable cause) {
            super(message, cause);
        }

        public RequestError(Throwable cause) {
            super(cause);
        }
    }
}
