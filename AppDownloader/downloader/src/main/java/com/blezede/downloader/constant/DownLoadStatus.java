package com.blezede.downloader.constant;

/**
 * com.blezede.downloader.constant
 * Time: 2019/4/9 15:35
 * Description:
 */
public class DownLoadStatus {

    /**
     * 等待下载或继续下载
     */
    public static final int STATUS_WAIT = 0;

    /**
     * 下载成功
     */
    public static final int STATUS_SUCCEED = 1;

    /**
     * 暂停下载
     */
    public static final int STATUS_PAUSED = 2;

    /**
     * 取消下载
     */
    public static final int STATUS_CANCELED = 3;

    /**
     * 下载失败
     */
    public static final int STATUS_FAILED = 4;

    /**
     * 下载中
     */
    public static final int STATUS_RUNNING = 5;

}
