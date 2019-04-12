package com.blezede.downloader.utils;

import android.net.Uri;

import java.io.File;

/**
 * com.blezede.downloader.utils
 * Time: 2019/4/12 10:49
 * Description:
 */
public class Common {

    public static String formatFileSize(long fileSize) {
        if (fileSize == 0) {
            return "0B";
        }
        if (fileSize < 1024) {
            return (double) fileSize + "B";
        } else if (fileSize < 1024 * 1024) {
            return (double) ((int) (fileSize / 1024. * 100)) / 100 + "KB";
        } else if (fileSize < 1024 * 1024 * 1024) {
            return (double) ((int) (fileSize / (1024. * 1024.) * 100)) / 100 + "MB";
        } else {
            return (double) ((int) (fileSize / (1024. * 1024. * 1024.) * 100)) / 100 + "GB";
        }
    }

    public static String toUtf8String(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0 && c <= 255) {
                sb.append(c);
            } else {
                byte[] b;
                try {
                    b = String.valueOf(c).getBytes("utf-8");
                } catch (Exception ex) {
                    b = new byte[0];
                }
                for (int j = 0; j < b.length; j++) {
                    int k = b[j];
                    if (k < 0)
                        k += 256;
                    sb.append("%" + Integer.toHexString(k).toUpperCase());
                }
            }
        }
        return sb.toString();
    }

    public static String getUrlQuery(String httpUrl) {
        Uri uri = Uri.parse(httpUrl);
        return uri.getQuery();
    }

    public static String getHttpUrlFileName(String httpUrl) {
        Uri uri = Uri.parse(httpUrl);
        String params = uri.getQuery();
        if (params != null && params.length() > 0) {
            String noParamsUrl = httpUrl.substring(0, httpUrl.lastIndexOf(params) - 1);
            return noParamsUrl.substring(noParamsUrl.lastIndexOf(File.separator) + 1);
        } else {
            return httpUrl.substring(httpUrl.lastIndexOf(File.separator) + 1);
        }
    }
}
