package com.blezede.downloader.utils;

import android.support.annotation.RequiresPermission;
import android.util.Log;

import static android.Manifest.permission.INTERNET;

/**
 * com.blezede.downloader.utils
 * Time: 2019/4/10 14:53
 * Description:
 */
public class NetWorkUtils {

    /**
     * Return whether network is available using ping.
     * <p>Must hold {@code <uses-permission android:name="android.permission.INTERNET" />}</p>
     *
     * @param ip The ip address.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isAvailableByPing(String ip) {
        if (ip == null || ip.length() <= 0) {
            ip = "223.5.5.5";// default ping ip
        }
        ShellUtils.CommandResult result = ShellUtils.execCmd(String.format("ping -c 1 %s", ip), false);
        boolean ret = result.result == 0;
        if (result.errorMsg != null) {
            Log.d("NetworkUtils", "isAvailableByPing() called" + result.errorMsg);
        }
        if (result.successMsg != null) {
            Log.d("NetworkUtils", "isAvailableByPing() called" + result.successMsg);
        }
        return ret;
    }
}
