package com.blezede.downloader.task;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.blezede.downloader.config.Config;
import com.blezede.downloader.constant.DownLoadStatus;
import com.blezede.downloader.interfaces.DownLoadListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * com.blezede.downloader
 * Time: 2019/4/9 14:27
 * Description:download task
 */
public class DownLoadTask implements Runnable, Handler.Callback {
    private static final String TAG = "DownLoadTask";

    private boolean mIsPause = false;
    private boolean mIsCancel = false;
    private Handler mHandler = new Handler(Looper.getMainLooper(), this);
    private int mCurrentStatus = DownLoadStatus.STATUS_WAIT;
    @NonNull
    private String mUrl;

    private long mContentLength = 0;

    private DownLoadListener mDownLoadListener;

    private boolean mIsDownLoading = false;

    private OkHttpClient mClient;

    private String mTargetDir;

    public DownLoadTask(@NonNull String url, DownLoadListener listener, @NonNull Config config) {
        this.mUrl = url;
        this.mDownLoadListener = listener;
        this.mClient = config.client;
        this.mTargetDir = config.targetDir;
        publishStatus(DownLoadStatus.STATUS_WAIT);
        if (this.mUrl.length() == 0) {
            publishStatus(DownLoadStatus.STATUS_FAILED);
        }
    }

    @Override
    public void run() {
        String name = mUrl.substring(mUrl.lastIndexOf("/"));

        long downloadedLength = 0;
        String directory = mTargetDir;
        File file = new File(directory + name);

        //判断任务是开始还是取消
        if (mIsCancel) {
            if (file.exists()) {
                file.delete();
            }
            publishStatus(DownLoadStatus.STATUS_CANCELED);
            return;
        }
        /*boolean available = NetWorkUtils.isAvailableByPing("");
        if (!available) {
            publishStatus(DownLoadStatus.STATUS_FAILED);
            return;
        }
*/
        if (file.exists()) {
            downloadedLength = file.length();
        }
        long contentLength = getContentLength(mUrl);
        mContentLength = contentLength;
        //无法下载
        if (contentLength == 0) {
            publishStatus(DownLoadStatus.STATUS_FAILED);
            return;
        } else if (contentLength == downloadedLength) {
            publishProgress(100);
            publishStatus(DownLoadStatus.STATUS_SUCCEED);
            return;
        }
        //开始下载
        mIsPause = false;
        mIsCancel = false;

        InputStream is = null;
        RandomAccessFile saveFile = null;
        Request request = new Request.Builder().
                addHeader("RANGE", "bytes=" + downloadedLength + "-") //指定从哪一个字节下载
                .url(mUrl).build();
        try {
            Response response = mClient.newCall(request).execute();
            //写入到本地
            if (response.body() != null) {
                Log.d(TAG, "doInBackground: response not null");
                is = response.body().byteStream();
                saveFile = new RandomAccessFile(file, "rw");
                saveFile.seek(downloadedLength);
                int len;
                byte[] buffer = new byte[1024 * 4];
                while ((len = is.read(buffer)) != -1) {
                    if (mIsPause) {
                        if (mCurrentStatus != DownLoadStatus.STATUS_PAUSED)
                            publishStatus(DownLoadStatus.STATUS_PAUSED);
                        response.close();
                        saveFile.close();
                        is.close();
                        return;
                    } else if (mIsCancel) {
                        if (file.exists()) {
                            file.delete();
                        }
                        publishStatus(DownLoadStatus.STATUS_CANCELED);
                        response.close();
                        saveFile.close();
                        is.close();
                        return;
                    }
                    if (!file.exists()) {
                        if (mCurrentStatus != DownLoadStatus.STATUS_FAILED)
                            publishStatus(DownLoadStatus.STATUS_FAILED);
                        response.close();
                        saveFile.close();
                        is.close();
                        return;
                    }
                    //获取已下载的进度
                    saveFile.write(buffer, 0, len);
                    downloadedLength += len;
                    int progress = (int) (downloadedLength * 100 / contentLength);
                    if (progress <= 0) {
                        progress = 0;
                    }
                    if (progress >= 100) {
                        progress = 100;
                    }
                    publishProgress(progress);
                }

                response.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (saveFile != null) {
                    saveFile.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (contentLength == downloadedLength) {
            publishStatus(DownLoadStatus.STATUS_SUCCEED);
            return;
        }
        publishStatus(DownLoadStatus.STATUS_FAILED);
    }

    private long getContentLength(String url) {
        long contentLength = 0;
        Request request = new Request.Builder().url(url).build();
        Response response = null;
        try {
            response = mClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response != null && response.isSuccessful()) {
            if (response.body() != null)
                contentLength = response.body().contentLength();
            response.close();
        } else {
            Log.d(TAG, "getContentLength: response null");
        }
        return contentLength;
    }

    public void pause() {
        mIsPause = true;
    }

    public void cancel() {
        mIsCancel = true;
    }

    public boolean isDownLoading() {
        return mIsDownLoading;
    }

    private void publishProgress(int progress) {
        Message message = Message.obtain();
        message.what = DownLoadStatus.STATUS_RUNNING;
        message.arg1 = progress;
        mHandler.sendMessage(message);
    }

    private void publishStatus(int status) {
        mHandler.sendEmptyMessage(status);
    }

    @Override
    public boolean handleMessage(Message msg) {
        mCurrentStatus = msg.what;
        switch (msg.what) {
            case DownLoadStatus.STATUS_WAIT:
                mIsDownLoading = false;
                if (mDownLoadListener != null) {
                    mDownLoadListener.onWait(mUrl);
                }
                break;
            case DownLoadStatus.STATUS_SUCCEED:
                mIsDownLoading = false;
                if (mDownLoadListener != null) {
                    mDownLoadListener.onSuccess(mUrl);
                }
                break;
            case DownLoadStatus.STATUS_CANCELED:
                mIsDownLoading = false;
                if (mDownLoadListener != null) {
                    mDownLoadListener.onCancel(mUrl);
                }
                break;
            case DownLoadStatus.STATUS_PAUSED:
                mIsDownLoading = false;
                if (mDownLoadListener != null) {
                    mDownLoadListener.onPause(mUrl);
                }
                break;
            case DownLoadStatus.STATUS_RUNNING:
                mIsDownLoading = true;
                if (mDownLoadListener != null) {
                    mDownLoadListener.onDownloading(mUrl, msg.arg1, mContentLength);
                }
                break;
            case DownLoadStatus.STATUS_FAILED:
                mIsDownLoading = false;
                if (mDownLoadListener != null) {
                    mDownLoadListener.onFailure(mUrl);
                }
                break;
        }
        return false;
    }
}
