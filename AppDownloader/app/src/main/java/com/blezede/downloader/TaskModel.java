package com.blezede.downloader;

import com.blezede.downloader.utils.Common;

/**
 * com.blezede.downloader
 * Time: 2019/4/10 15:39
 * Description:
 */
public class TaskModel {

    /**
     * 初始状态
     */
    public static final int IDEL = 0;
    public static final int PAUSE = 1;
    public static final int DOWNLOADING = 2;
    public static final int CANCLE = 3;
    public static final int SUCCESS = 4;
    public static final int FAILED = 5;

    /**
     * 等待状态
     */
    public static final int WAIT = 6;

    String name;

    public String url;

    public int progress = 0;

    public int status = IDEL;

    public boolean isDownLoading = false;

    public int percent = 0;

    public int currSize = 0;

    public long totalSize = 0;

    public TaskModel(String url) {
        this.url = url;
        this.name = Common.getHttpUrlFileName(url);
    }
}
