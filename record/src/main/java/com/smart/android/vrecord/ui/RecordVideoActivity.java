package com.smart.android.vrecord.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smart.android.vrecord.R;
import com.smart.android.vrecord.VideoRecordPicker;
import com.smart.android.vrecord.camera2.AutoFitTextureView;
import com.smart.android.vrecord.camera2.CameraVideo;
import com.smart.android.vrecord.camera2.CameraVideoManager;

import java.io.File;
import java.util.Locale;

public class RecordVideoActivity extends AppCompatActivity {

    private static final String TAG = "camera";

    private RelativeLayout llCover;
    private Button btnSuspend;
    private ImageView ivRecord, ivFacing;

    /**
     * 标题
     **/
    private TextView mTitleView;
    /**切换前后摄像头  录制的时候隐藏掉 切换摄像头就是重新初始化摄像机**/
    /**
     * 预览画面
     **/
    private AutoFitTextureView mTextureView;
    /**
     * 录制视频的manager
     **/
    private CameraVideo mCameraVideo;
    /**
     * 视频存放路径
     **/
    private String mVideoPath = null;
    /**
     * 录制了多长时间
     **/
    private int mCurrentDuration;
    /**
     * 录制视频的进度
     **/
    private ProgressBar mProgressBar;
    /**
     * 录制视频时候的计时动画
     **/
    private ObjectAnimator mAnimator;
    /**
     * 是否正在录制视频
     **/
    private boolean isRecording;

    /**
     * 暂停中
     */
    private boolean isSuspend;

    /**
     * act不在被覆盖时 是否执行 mCameraVideo.onPause()；
     */
    private boolean is2Pause;

    /**
     * 是否有暂停能力
     */
    private boolean canSuspend;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        setContentView(R.layout.activity_record_video);
        initUI();
    }


    private void initUI() {
        llCover = findViewById(R.id.ll_cover);
        ivRecord = findViewById(R.id.iv_record);
        mTitleView = findViewById(R.id.tv_title);
        mTextureView = findViewById(R.id.textureView);
        btnSuspend = findViewById(R.id.btn_suspend);
        ivFacing = findViewById(R.id.iv_facing);
        canSuspend = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
        btnSuspend.setOnClickListener(v -> {
            if (!canSuspend)
                return;
            if (isSuspend) {
                mCameraVideo.resumeRecordVideo();
                btnSuspend.setText("暂停");
            } else {
                mCameraVideo.pauseRecordVideo();
                btnSuspend.setText("继续");
            }
            isSuspend = !isSuspend;
        });
        ivRecord.setOnClickListener(v -> {
            if (isRecording) {
                Log.e("sss", "录制完成");
                stopRecord();
            } else {
                startRecord();
            }
        });
        ivFacing.setOnClickListener(v -> {
            mCameraVideo.switchCameraFacing();
        });
        findViewById(R.id.tv_cancle).setOnClickListener(v -> reStartPreview());
        findViewById(R.id.tv_pick).setOnClickListener(v -> {
            if (VideoRecordPicker.getInstance().getFinishListener() != null) {
                VideoRecordPicker.getInstance().getFinishListener().onFinishListener(mVideoPath);
            }
            finish();
        });
        findViewById(R.id.video_play).setOnClickListener(v -> {
            is2Pause = true;
            VideoPlayActivity.start(RecordVideoActivity.this, mVideoPath);
        });


        mCameraVideo = new CameraVideoManager(this);
        mCameraVideo.setAutoFitTextureView(mTextureView);
        mCameraVideo.setOnRecordFinishListener(() -> stopRecord());
        mCameraVideo.setOnProgressChangeListener(duration -> {
            mCurrentDuration = duration;
            int minute = duration / 60;
            int second = duration % 60;
            mTitleView.setText(String.format(Locale.getDefault(),
                    "%02d:%02d", minute, second));
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!is2Pause) {
            mCameraVideo.onResume();
            if (mAnimator != null) {
                mAnimator.resume();
            }
        }
        is2Pause = false;

    }

    @Override
    public void onPause() {
        super.onPause();
        if (!is2Pause) {
            mCameraVideo.onPause();
            rest();
        }
    }

    private void reStartPreview() {
        rest();
        mCameraVideo.startPreview();
    }


    private void stopRecord() {
        rest();
        mCameraVideo.stopRecordingVideo();
        llCover.setVisibility(View.VISIBLE);
    }

    private void startRecord() {
        isRecording = true;
        ivRecord.setImageResource(R.drawable.image_record_pause);
        if (canSuspend)
            btnSuspend.setVisibility(View.VISIBLE);
        ivFacing.setVisibility(View.GONE);
        llCover.setVisibility(View.GONE);
        mVideoPath = getVideoFilePath(RecordVideoActivity.this);
        Log.e("path", mVideoPath);
        mCameraVideo.startRecordingVideo(mVideoPath);
        mTitleView.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.lv_timer_dot, 0, 0, 0);
        if (mAnimator == null) {
            mAnimator = ObjectAnimator.ofFloat(mTitleView, "alpha",
                    1, 0.4f, 1)
                    .setDuration(1000);

            mAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            mAnimator.setRepeatMode(ObjectAnimator.REVERSE);
        }
        mAnimator.start();
    }

    /*重置*/
    private void rest() {
        isRecording = false;
        isSuspend = false;
        ivRecord.setImageResource(R.drawable.image_record_start);
        mTitleView.setText("");
        mTitleView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        if (canSuspend)
            btnSuspend.setVisibility(View.GONE);
        ivFacing.setVisibility(View.VISIBLE);
        llCover.setVisibility(View.GONE);
        if (mAnimator != null) {
            mAnimator.cancel();
        }
    }

    private String getVideoFilePath(Context context) {
        final File dir = new File(context.getExternalFilesDir(null), "Videos");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return (dir.getAbsolutePath() + "/") + System.currentTimeMillis() + ".mp4";
    }

}
