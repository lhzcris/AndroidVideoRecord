package com.smart.android.vrecord.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import com.smart.android.vrecord.OnRecordFinishListener;
import com.smart.android.vrecord.R;
import com.smart.android.vrecord.VideoRecordPicker;

public class VideoPlayActivity extends AppCompatActivity {

    private VideoView mVideoView;


    public static void start(Context context, String path) {
        Intent starter = new Intent(context, VideoPlayActivity.class);
        starter.putExtra("video_path", path);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        setContentView(R.layout.activity_video_play);

        mVideoView = findViewById(R.id.video_view);
        initVideo(getIntent().getStringExtra("video_path"));
    }


    private void initVideo(String path) {
        if (TextUtils.isEmpty(path))
            return;
        MediaController controller = new MediaController(VideoPlayActivity.this);
        mVideoView.setMediaController(controller);
        mVideoView.setVideoPath(path);
        mVideoView.setOnCompletionListener(mp -> VideoPlayActivity.this.finish());
        mVideoView.start();
    }
}
