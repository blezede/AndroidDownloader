package com.blezede.downloader.interfaces;

/**
 * com.blezede.downloader.interfaces
 * Time: 2019/4/10 10:13
 * Description:
 */
public interface Observer {

    void onWait(String url);

    void onSuccess(String url);

    void onFailure(String url);

    void onPause(String url);

    void onCancel(String url);

    void onDownloading(String url, int progress, long totalLength);
}
