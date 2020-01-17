package com.smart.android.vrecord.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.MediaController;
import android.widget.VideoView;

import com.smart.android.vrecord.R;

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
        mVideoView.start();
    }
}
