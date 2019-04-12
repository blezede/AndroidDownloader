package com.blezede.downloader.interfaces;

/**
 * com.blezede.downloader.interfaces
 * Time: 2019/4/9 16:06
 * Description:
 */
public interface DownLoadListener {

    void onSuccess(String url);

    void onFailure(String url);

    void onPause(String url);

    void onCancel(String url);

    void onDownloading(String url, int progress, long totalLength);

    void onWait(String mUrl);
}
