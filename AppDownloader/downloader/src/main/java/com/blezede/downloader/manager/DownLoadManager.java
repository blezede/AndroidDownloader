package com.blezede.downloader.manager;

import android.os.Environment;
import android.util.Log;

import com.blezede.downloader.config.Config;
import com.blezede.downloader.interfaces.DownLoadListener;
import com.blezede.downloader.interfaces.IDownLoadManager;
import com.blezede.downloader.interfaces.Observer;
import com.blezede.downloader.task.DownLoadTask;
import com.blezede.downloader.utils.Common;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * com.blezede.downloader.manager
 * Time: 2019/4/9 15:59
 * Description:
 */
public class DownLoadManager implements IDownLoadManager {

    private static final String TAG = "DownLoadManager";
    public static boolean DEBUG = false;
    private Map<String, DownLoadTask> mTasks = new HashMap<>();
    private static IDownLoadManager sDownLoadManager;
    private Observer mObserver;
    private Config mConfig;

    private DownLoadManager() {
    }

    private DownLoadManager(Build build) {
        mConfig = new Config();
        if (build != null && build.executorService != null) {
            mConfig.threadPool = build.executorService;
        } else
            mConfig.threadPool = Executors.newFixedThreadPool(Math.min(Runtime.getRuntime().availableProcessors() * 2 + 1, 3));
        if (build != null && build.targetDir != null && build.targetDir.length() > 0) {
            mConfig.targetDir = build.targetDir;
        } else
            mConfig.targetDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator;
        if (build != null && build.okHttpClient != null) {
            mConfig.client = build.okHttpClient;
        } else {
            mConfig.client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build();
        }
    }

    public static IDownLoadManager get() {
        if (sDownLoadManager == null) {
            synchronized (DownLoadManager.class) {
                if (sDownLoadManager == null) {
                    sDownLoadManager = new DownLoadManager(null);
                }
            }
        }
        return sDownLoadManager;
    }

    @Override
    public void pause(String url) {
        if (!mTasks.containsKey(url)) {
            return;
        }
        DownLoadTask task = mTasks.get(url);
        task.pause();
    }

    @Override
    public void resume(String url) {
        addTask(url);
    }

    @Override
    public void cancel(String url) {
        if (!mTasks.containsKey(url)) {
            //已经暂停或未开始下载
            String name = Common.getHttpUrlFileName(url);
            String targetDir = mConfig.targetDir;
            if (targetDir.lastIndexOf(File.separator) != targetDir.length() - 1) {
                targetDir = targetDir + File.separator;
            }
            File file = new File(targetDir + name);
            if (file.exists()) file.delete();
            if (mObserver != null) mObserver.onCancel(url);
            return;
        }
        DownLoadTask task = mTasks.get(url);
        task.cancel();
    }

    @Override
    public void subscribe(Observer observer) {
        this.mObserver = observer;
    }

    @Override
    public void newTask(String url) {
        addTask(url);
    }

    @Override
    public IDownLoadManager newTasks(List<String> urls) {
        addTask(urls);
        return this;
    }

    @Override
    public boolean isDownLoading(String url) {
        return mTasks.containsKey(url) && mTasks.get(url).isDownLoading();
    }

    private void addTask(List<String> urls) {
        for (String url : urls) {
            if (mTasks.containsKey(url)) {
                continue;
            }
            addTask(url);
        }
    }

    private void addTask(String url) {
        if (mTasks.containsKey(url)) {
            return;
        }
        DownLoadTask task = new DownLoadTask(url, new DownLoadListener() {

            @Override
            public void onWait(String url) {
                if (mObserver != null) mObserver.onWait(url);
            }

            @Override
            public void onSuccess(String url) {
                mTasks.remove(url);
                if (mObserver != null) mObserver.onSuccess(url);
                if (DEBUG) {
                    Log.d(TAG, "onSuccess --> " + url);
                }
            }

            @Override
            public void onFailure(String url) {
                mTasks.remove(url);
                if (mObserver != null) mObserver.onFailure(url);
                if (DEBUG) {
                    Log.d(TAG, "onFailure --> " + url);
                }
            }

            @Override
            public void onPause(String url) {
                mTasks.remove(url);
                if (mObserver != null) mObserver.onPause(url);
                if (DEBUG) {
                    Log.d(TAG, "onPause --> " + url);
                }
            }

            @Override
            public void onCancel(String url) {
                mTasks.remove(url);
                if (mObserver != null) mObserver.onCancel(url);
                if (DEBUG) {
                    Log.d(TAG, "onCancel --> " + url);
                }
            }

            @Override
            public void onDownloading(String url, int progress, long totalLength) {
                if (mObserver != null) mObserver.onDownloading(url, progress, totalLength);
                if (DEBUG) {
                    Log.d(TAG, "onDownloading --> " + url + "---" + progress + "---" + totalLength);
                }
            }
        }, mConfig);
        mTasks.put(url, task);
        mConfig.threadPool.submit(task);
    }

    public static class Build {
        private ExecutorService executorService;
        private String targetDir;
        private OkHttpClient okHttpClient;

        public Build executor(ExecutorService executorService) {
            this.executorService = executorService;
            return this;
        }

        public Build targetDir(String targetDir) {
            this.targetDir = targetDir;
            return this;
        }

        public Build okHttpClient(OkHttpClient client) {
            this.okHttpClient = client;
            return this;
        }

        public IDownLoadManager build() {
            return new DownLoadManager(this);
        }
    }
}
