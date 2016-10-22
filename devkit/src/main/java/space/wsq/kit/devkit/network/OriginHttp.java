package space.wsq.kit.devkit.network;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

import space.wsq.kit.devkit.ILogger;
import space.wsq.kit.devkit.SafeUtil;
import space.wsq.kit.devkit.cache.DiskCacheFacade;

class OriginHttp extends BaseHttp {
    private static final String TAG = "OriginHttp";
    // connection timeout is 30s
    private static final int CONNECT_TIMEOUT = 30000;
    // read timeout is 60s
    private static final int READ_TIMEOUT = 60000;
    private final ExecutorService executor;
    private final DiskCacheFacade cache;

    OriginHttp(Context context) {
        this.executor = Executors.newCachedThreadPool();
        this.cache = new DiskCacheFacade(context);
    }

    private HttpURLConnection openConnection(String u, int timeout, boolean doOutput, String method) {
        URL url;
        try {
            url = new URL(u);
        } catch (MalformedURLException e) {
            ILogger.defaultLogger.e(TAG, "failed to resolve url " + u, e);
            return null;
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(timeout);//设置连接超时时间
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setDoInput(true);//打开输入流，以便从服务器获取数据
            connection.setDoOutput(doOutput);//打开输出流，以便向服务器提交数据
            connection.setRequestMethod(method);
            if ("POST".equals(method)) {
                connection.setUseCaches(false);
            } else if ("GET".equals(method)) {
                connection.setUseCaches(true);
            }
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        } catch (IOException e) {
            ILogger.defaultLogger.e(TAG, "failed to connect to url " + u, e);
            if (connection != null) {
                connection.disconnect();
            }
        }
        return connection;
    }

    private InputStream unzipStream(HttpURLConnection connection) throws IOException {
        String encoding = connection.getContentEncoding();
        InputStream stream = connection.getInputStream();
        return (encoding != null && encoding.equalsIgnoreCase("gzip")) ? new GZIPInputStream(stream) : stream;
    }

    @Override
    public void execute(final Request request, final StreamListener listener) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                // fast cache from local cache system.
                if (request.canUseCache()) {
                    InputStream stream = cache.get(request.url.toString());
                    if (stream != null) {
                        listener.onResponse(new BufferedInputStream(stream));
                        return;
                    }
                }
                innerExecute(request, listener);
            }});
        }

    private void innerExecute(Request request, StreamListener listener) {
        String url = request.url.toString();
        byte[] data;
        try {
            data = request.hasBody() ? encodeBody(request.body, "utf-8").toString().getBytes() : null;
        } catch (UnsupportedEncodingException e) {
            listener.onErrorResponse(e);
            return;
        }
        HttpURLConnection connection = openConnection(url, CONNECT_TIMEOUT, request.hasBody(), request.method);
        if (connection == null) {
            listener.onErrorResponse(new Exception("Unable to open connection"));
            return;
        }
        try {
            addHeader(connection, request);
        } catch (UnsupportedEncodingException e) {
            listener.onErrorResponse(e);
            return;
        } finally {
            connection.disconnect();
        }
        // use gzip as default encoding
        connection.addRequestProperty("Accept-Encoding", "gzip");
        boolean hasInputStream = false;
        try {
            if (data != null) {
                connection.setRequestProperty("Content-Length", String.valueOf(data.length));
                OutputStream outputStream = connection.getOutputStream();
                try {
                    outputStream.write(data, 0, data.length);
                } finally {
                    SafeUtil.safeClose(outputStream);
                }
            }
            int response = connection.getResponseCode();//获得服务器的响应码
            if (response == HttpURLConnection.HTTP_OK) {
                InputStream is = new BufferedInputStream(unzipStream(connection));
                hasInputStream = true;
                try {
                    listener.onResponse(is);
                    if (request.shouldSaveCache()) {
                        is.reset();
                        cache.put(request.url.toString(), is);
                    }
                } finally {
                    SafeUtil.safeClose(is);
                }
            }
        } catch (IOException e) {
            if (!hasInputStream) {
                listener.onErrorResponse(e);
            }
        } finally {
            connection.disconnect();
        }
    }

    static void addHeader(HttpURLConnection connection, Request request) throws UnsupportedEncodingException {
        if (request.headers == null || request.headers.size() == 0) {
            return;
        }

        for (Map.Entry<String, String> entry : request.headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), URLEncoder.encode(entry.getValue(), "utf-8"));
        }
    }

    static StringBuffer encodeBody(Map<String, String> params, String encode) throws UnsupportedEncodingException {
        StringBuffer buffer = new StringBuffer();//存储封装好的请求体信息
        for (Map.Entry<String, String> entry : params.entrySet()) {
            buffer.append(entry.getKey())
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), encode))
                    .append("&");
        }
        buffer.deleteCharAt(buffer.length() - 1);//删除最后的一个"&"
        return buffer;
    }
}
