package com.blezede.downloader;

import android.Manifest;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blezede.downloader.interfaces.IDownLoadManager;
import com.blezede.downloader.interfaces.Observer;
import com.blezede.downloader.manager.DownLoadManager;
import com.blezede.downloader.utils.Common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements Observer {
    private static final String URL2 = "http://m.down.sandai.net/MobileThunder/Android_5.34.2.4700/XLWXguanwang.apk";
    private static final String URL1 = "http://s1.music.126.net/download/android/CloudMusic_official_4.0.0_179175.apk";
    private static final String URL3 = "https://qd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk";
    private static final String URL4 = "http://dldir1.qq.com/weixin/android/weixin703android1400.apk";
    private static final String URL5 = "http://gdown.baidu.com/data/wisegame/ff4efd277de65cb8/weibo_3854.apk";
    private static final String URL6 = "http://download.anfensi.com/apk/bilibiliapp_anfensi.com.apk";
    private static final String URL7 = "http://gdown.baidu.com/data/wisegame/a6486d3de30f27e4/aiqiyijisuban_81000.apk";
    List<TaskModel> list = new ArrayList<>();
    RecyclerView mRecyclerView;
    IDownLoadManager mDownLoadManager;
    DownLoadAdapter mDownLoadAdapter;
    LinearLayoutManager mLinearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET}, 1);
        list.add(new TaskModel(URL1));
        list.add(new TaskModel(URL2));
        list.add(new TaskModel(URL3));
        list.add(new TaskModel(URL4));
        list.add(new TaskModel(URL5));
        list.add(new TaskModel(URL6));
        list.add(new TaskModel(URL7));
        mRecyclerView = findViewById(R.id.recycler_view);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mDownLoadAdapter = new DownLoadAdapter();
        mRecyclerView.setAdapter(mDownLoadAdapter);
        mDownLoadManager = DownLoadManager.get();
        mDownLoadManager.subscribe(this);
        //或者配置线程池及httpClient、目标目录
        //mDownLoadManager = new DownLoadManager.Build()
        //        .executor(Executors.newSingleThreadExecutor())
        //        .targetDir(this.getExternalCacheDir().getAbsolutePath() + File.separator)
        //        .build();
        //mDownLoadManager.subscribe(this);
    }

    @Override
    public void onWait(String url) {
        int position = getPosition(url);
        list.get(position).isDownLoading = false;
        list.get(position).progress = 0;
        list.get(position).status = TaskModel.WAIT;
        mDownLoadAdapter.notifyItemChanged(position);
    }

    @Override
    public void onSuccess(String url) {
        int position = getPosition(url);
        list.get(position).isDownLoading = false;
        list.get(position).status = TaskModel.SUCCESS;
        mDownLoadAdapter.notifyItemChanged(position);
    }

    @Override
    public void onFailure(String url) {
        int position = getPosition(url);
        list.get(position).isDownLoading = false;
        list.get(position).progress = -1;
        list.get(position).status = TaskModel.FAILED;
        mDownLoadAdapter.notifyItemChanged(position);
    }

    @Override
    public void onPause(String url) {
        int position = getPosition(url);
        list.get(position).isDownLoading = false;
        list.get(position).status = TaskModel.PAUSE;
        mDownLoadAdapter.notifyItemChanged(position);
    }

    @Override
    public void onCancel(String url) {
        int position = getPosition(url);
        list.get(position).isDownLoading = false;
        list.get(position).progress = 0;
        list.get(position).percent = 0;
        list.get(position).totalSize = 0;
        list.get(position).currSize = 0;
        list.get(position).status = TaskModel.CANCLE;
        mDownLoadAdapter.notifyItemChanged(position);
    }

    @Override
    public void onDownloading(String url, int progress, long totalLength) {
        int position = getPosition(url);
        TaskModel taskModel = list.get(position);
        taskModel.progress = progress;
        taskModel.isDownLoading = true;
        taskModel.percent = progress;
        taskModel.totalSize = totalLength;
        taskModel.currSize = (int) (progress / 100. * totalLength);
        list.get(position).status = TaskModel.DOWNLOADING;
        if (mLinearLayoutManager.findFirstVisibleItemPosition() <= position && mLinearLayoutManager.findLastVisibleItemPosition() >= position) {
            DownLoadAdapter.Holder holder = (DownLoadAdapter.Holder) mRecyclerView.findViewHolderForAdapterPosition(position);
            if (holder != null) {
                holder.progressBar.setProgress(progress);
                if (!holder.statusBtn.getText().equals(getString(R.string.pause)))
                    holder.statusBtn.setText(R.string.pause);
                holder.percent.setText(Common.formatFileSize(taskModel.currSize) + "--" + Common.formatFileSize(taskModel.totalSize) + "    " + taskModel.percent + "%");
            }
        }
    }

    int getPosition(String url) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).url.equals(url)) {
                return i;
            }
        }
        return 0;
    }

    class DownLoadAdapter extends RecyclerView.Adapter<DownLoadAdapter.Holder> {


        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new Holder(LayoutInflater.from(MainActivity.this).inflate(R.layout.recycler_item, null));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int i) {
            TaskModel taskModel = list.get(i);
            holder.fileName.setText(taskModel.name);
            holder.percent.setText(Common.formatFileSize(taskModel.currSize) + "--" + Common.formatFileSize(taskModel.totalSize) + "    " + taskModel.percent + "%");
            if (taskModel.progress > 0 && taskModel.progress <= 100) {
                if (holder.progressBar.getProgress() != taskModel.progress)
                    holder.progressBar.setProgress(taskModel.progress);
            } else if (taskModel.progress == 0) {
                if (holder.progressBar.getProgress() != 0)
                    holder.progressBar.setProgress(0);
            }
            switch (taskModel.status) {
                case TaskModel.IDEL:
                    if (!holder.statusBtn.getText().equals(getString(R.string.start)))
                        holder.statusBtn.setText(R.string.start);
                    break;
                case TaskModel.DOWNLOADING:
                    if (!holder.statusBtn.getText().equals(getString(R.string.pause)))
                        holder.statusBtn.setText(R.string.pause);
                    break;
                case TaskModel.CANCLE:
                    if (!holder.statusBtn.getText().equals(getString(R.string.canceled)))
                        holder.statusBtn.setText(R.string.canceled);
                    break;
                case TaskModel.FAILED:
                    if (!holder.statusBtn.getText().equals(getString(R.string.failed)))
                        holder.statusBtn.setText(R.string.failed);
                    break;
                case TaskModel.WAIT:
                    if (!holder.statusBtn.getText().equals(getString(R.string.wait)))
                        holder.statusBtn.setText(R.string.wait);
                    break;
                case TaskModel.PAUSE:
                    if (!holder.statusBtn.getText().equals(getString(R.string.resume)))
                        holder.statusBtn.setText(R.string.resume);
                    break;
                case TaskModel.SUCCESS:
                    if (!holder.statusBtn.getText().equals(getString(R.string.success)))
                        holder.statusBtn.setText(R.string.success);
                    if (holder.cancelBtn.getVisibility() == View.VISIBLE) {
                        holder.cancelBtn.setVisibility(View.INVISIBLE);
                    }
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class Holder extends RecyclerView.ViewHolder {

            public ProgressBar progressBar;
            public Button statusBtn;
            public Button cancelBtn;
            public TextView fileName;
            public TextView percent;

            public Holder(@NonNull View itemView) {
                super(itemView);
                progressBar = itemView.findViewById(R.id.progress_bar);
                statusBtn = itemView.findViewById(R.id.status_btn);
                cancelBtn = itemView.findViewById(R.id.cancel_btn);
                fileName = itemView.findViewById(R.id.file_name);
                percent = itemView.findViewById(R.id.percent);
                statusBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        TaskModel taskModel = list.get(position);
                        if (taskModel.status == TaskModel.IDEL) {
                            mDownLoadManager.newTask(taskModel.url);
                        } else if (taskModel.status == TaskModel.DOWNLOADING) {
                            mDownLoadManager.pause(taskModel.url);
                        } else if (taskModel.status == TaskModel.PAUSE) {
                            mDownLoadManager.resume(taskModel.url);
                        }
                    }
                });
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        TaskModel taskModel = list.get(position);
                        if (taskModel.status == TaskModel.WAIT || taskModel.status == TaskModel.DOWNLOADING || taskModel.status == TaskModel.FAILED || taskModel.status == TaskModel.PAUSE) {
                            mDownLoadManager.cancel(list.get(position).url);
                        }
                    }
                });
            }
        }
    }
}
