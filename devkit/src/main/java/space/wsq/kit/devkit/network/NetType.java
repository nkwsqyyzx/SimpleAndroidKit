package space.wsq.kit.devkit.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

@SuppressWarnings({"unused"})
public class NetType {
    public static final int TYPE_NONE = -1;
    public static final int TYPE_WIFI = ConnectivityManager.TYPE_WIFI;
    public static final int TYPE_MOBILE = ConnectivityManager.TYPE_MOBILE;

    /**
     * NETWORK subtype:Unknown, wifi, 2G, 3G, 4G
     */
    public static final int NETWORK_CLASS_UNKNOWN = 0;
    /**
     * Class of broadly defined "WIFI" networks.
     */
    public static final int NETWORK_CLASS_WIFI = 1;
    /**
     * Class of broadly defined "2G" networks.
     */
    public static final int NETWORK_CLASS_2_G = 2;
    /**
     * Class of broadly defined "3G" networks.
     */
    public static final int NETWORK_CLASS_3_G = 3;
    /**
     * Class of broadly defined "4G" networks.
     */
    public static final int NETWORK_CLASS_4_G = 4;

    private final int netType;
    private final int subType;

    public NetType(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager == null ? null : manager.getActiveNetworkInfo();
        this.netType = info == null ? TYPE_NONE : info.getType();
        this.subType = netType == TYPE_MOBILE ? info.getSubtype() : NETWORK_TYPE_UNKNOWN;
    }

    public int subType() {
        switch (netType) {
            case TYPE_WIFI:
                return NETWORK_CLASS_WIFI;
            case TYPE_NONE:
                return NETWORK_CLASS_UNKNOWN;
            case TYPE_MOBILE:
                return subNetType();
            default:
                throw new IllegalStateException("Unexpected network type.");
        }
    }

    public boolean isAvailable() {
        return netType != TYPE_NONE;
    }

    public boolean isWifi() {
        return subType() == NETWORK_CLASS_WIFI;
    }

    public boolean is2G() {
        return subType() == NETWORK_CLASS_2_G;
    }

    public boolean is3G() {
        return subType() == NETWORK_CLASS_3_G;
    }

    public boolean is4G() {
        return subType() == NETWORK_CLASS_4_G;
    }

    private int subNetType() {
        switch (subType) {
            case NETWORK_TYPE_GPRS:
            case NETWORK_TYPE_EDGE:
            case NETWORK_TYPE_CDMA:
            case NETWORK_TYPE_1xRTT:
            case NETWORK_TYPE_IDEN:
                return NETWORK_CLASS_2_G;
            case NETWORK_TYPE_UMTS:
            case NETWORK_TYPE_EVDO_0:
            case NETWORK_TYPE_EVDO_A:
            case NETWORK_TYPE_HSDPA:
            case NETWORK_TYPE_HSUPA:
            case NETWORK_TYPE_HSPA:
            case NETWORK_TYPE_EVDO_B:
            case NETWORK_TYPE_EHRPD:
            case NETWORK_TYPE_HSPAP:
                return NETWORK_CLASS_3_G;
            case NETWORK_TYPE_LTE:
                return NETWORK_CLASS_4_G;
            default:
                return NETWORK_CLASS_4_G;
        }
    }

    /**
     * network sub type.
     */
    private static final int NETWORK_TYPE_UNKNOWN = 0;
    /**
     * Current network is GPRS
     */
    private static final int NETWORK_TYPE_GPRS = 1;
    /**
     * Current network is EDGE
     */
    private static final int NETWORK_TYPE_EDGE = 2;
    /**
     * Current network is UMTS
     */
    private static final int NETWORK_TYPE_UMTS = 3;
    /**
     * Current network is CDMA: Either IS95A or IS95B
     */
    private static final int NETWORK_TYPE_CDMA = 4;
    /**
     * Current network is EVDO revision 0
     */
    private static final int NETWORK_TYPE_EVDO_0 = 5;
    /**
     * Current network is EVDO revision A
     */
    private static final int NETWORK_TYPE_EVDO_A = 6;
    /**
     * Current network is 1xRTT
     */
    private static final int NETWORK_TYPE_1xRTT = 7;
    /**
     * Current network is HSDPA
     */
    private static final int NETWORK_TYPE_HSDPA = 8;
    /**
     * Current network is HSUPA
     */
    private static final int NETWORK_TYPE_HSUPA = 9;
    /**
     * Current network is HSPA
     */
    private static final int NETWORK_TYPE_HSPA = 10;
    /**
     * Current network is iDen
     */
    private static final int NETWORK_TYPE_IDEN = 11;
    /**
     * Current network is EVDO revision B
     */
    private static final int NETWORK_TYPE_EVDO_B = 12;
    /**
     * Current network is LTE
     */
    private static final int NETWORK_TYPE_LTE = 13;
    /**
     * Current network is eHRPD
     */
    private static final int NETWORK_TYPE_EHRPD = 14;
    /**
     * Current network is HSPA+
     */
    private static final int NETWORK_TYPE_HSPAP = 15;
}
