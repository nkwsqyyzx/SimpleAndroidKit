package space.wsq.kit.devkit.network;

import android.text.TextUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

abstract class BaseHttp implements IHttp {

    static URI appendParameters(URI u, Map<String, String> map) throws URISyntaxException {
        if (map == null || map.size() == 0) {
            return u;
        }
        StringBuilder sb = new StringBuilder(TextUtils.isEmpty(u.getQuery()) ? "" : u.getQuery());
        if (sb.length() > 0) {
            sb.append('&');
        }
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue());
        }
        return new URI(u.getScheme(), u.getAuthority(), u.getPath(), sb.toString(), u.getFragment());
    }
}
