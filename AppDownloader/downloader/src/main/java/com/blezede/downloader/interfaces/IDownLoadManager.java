package com.blezede.downloader.interfaces;

import java.util.List;

/**
 * com.blezede.downloader.interfaces
 * Time: 2019/4/9 15:59
 * Description:
 */
public interface IDownLoadManager {

    void pause(String url);

    void resume(String url);

    void cancel(String url);

    void subscribe(Observer observer);

    void newTask(String url);

    IDownLoadManager newTasks(List<String> urls);

    boolean isDownLoading(String url);
}
