package space.wsq.kit.devkit;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

@SuppressWarnings({"unused"})
public class DeviceUtil implements DeviceConstants {
    private static final String TAG = "DeviceUtil";
    private static boolean mHasGetCpuArchitecture;
    private static boolean mIsArmV7;

    public static boolean isArmV7() {
        if (mHasGetCpuArchitecture) {
            return mIsArmV7;
        }
        mHasGetCpuArchitecture = true;
        InputStream is = null;
        InputStreamReader ir = null;
        BufferedReader br = null;
        try {
            is = new FileInputStream("/proc/cpuinfo");
            ir = new InputStreamReader(is);
            br = new BufferedReader(ir);
            Object[] mArmArchitecture = {null, -1, -1};
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                String[] pair = line.split(":");
                if (pair.length != 2) {
                    continue;
                }

                String key = pair[0].trim();
                String val = pair[1].trim();
                if ("Processor".equalsIgnoreCase(key)) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = val.indexOf("ARMv") + 4; i < val.length(); i++) {
                        String temp = val.charAt(i) + "";
                        if (temp.matches("\\d")) {
                            sb.append(temp);
                        } else {
                            break;
                        }
                    }
                    mArmArchitecture[0] = "ARM";
                    mArmArchitecture[1] = TypeUtil.convertToInt(sb.toString(), -1);
                    continue;
                }

                if ("Features".equalsIgnoreCase(key)) {
                    if (val.contains("neon")) {
                        mArmArchitecture[2] = "neon";
                    }
                    continue;
                }

                if ("model name".equalsIgnoreCase(key)) {
                    if (val.contains("Intel")) {
                        mArmArchitecture[0] = "INTEL";
                        mArmArchitecture[2] = "atom";
                    }
                    continue;
                }

                if ("cpu family".equalsIgnoreCase(key)) {
                    mArmArchitecture[1] = TypeUtil.convertToInt(val, -1);
                    continue;
                }
            }

            mIsArmV7 = ((Integer) mArmArchitecture[1]) == 7;
        } catch (Exception e) {
            ILogger.defaultLogger.e(TAG, "failed to check processor", e);
        } finally {
            SafeUtil.safeClose(br);
            SafeUtil.safeClose(ir);
            SafeUtil.safeClose(is);
        }

        return mIsArmV7;
    }

    @SuppressWarnings("deprecation")
    public static String[] archTypes() {
        if (Build.VERSION.SDK_INT < 8) {
            return new String[]{Build.CPU_ABI};
        }
        if (Build.VERSION.SDK_INT >= 8 && Build.VERSION.SDK_INT < 21) {
            return new String[]{Build.CPU_ABI, Build.CPU_ABI2};
        } else {
            try {
                return Reflection.getField(null, Build.class, "SUPPORTED_ABIS");
            } catch (Throwable e) {
                return new String[]{Build.CPU_ABI, Build.CPU_ABI2};
            }
        }
    }

    public static int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            ILogger.defaultLogger.e(TAG, "failed to read package info", e);
        }
        return 1;
    }
}
