package com.smart.android.vrecord.camera2;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smart.android.vrecord.R;

import java.io.File;

public class RecordVideoActivity extends AppCompatActivity {

    private static final String TAG = "camera";
    /**
     * 从相册选取
     **/
    private final static int REQUEST_VIDEO_CODE = 1010;
    /**标题**/
//    private TextView mTitleView;
    /**切换前后摄像头  录制的时候隐藏掉 切换摄像头就是重新初始化摄像机**/
//    private ImageView mCameraFacing;
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

    public static int getScreenWidth(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        setContentView(R.layout.activity_record_video);
        mTextureView = findViewById(R.id.textureView);

        findViewById(R.id.btn_record).setOnClickListener(v -> {
            startRecord();
        });


//        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mTextureView.getLayoutParams();
//        lp.height = getScreenWidth(this) * 16 / 9;
//        mTextureView.setLayoutParams(lp);


        mCameraVideo = new CameraVideoManager(this);
        mCameraVideo.setAutoFitTextureView(mTextureView);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mCameraVideo.onResume();
        if (mAnimator != null) {
            mAnimator.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mCameraVideo.onPause();
        if (mAnimator != null) {
            mAnimator.pause();
        }
    }


    private void stopRecord() {
        isRecording = false;
        mCameraVideo.stopRecordingVideo();
        if (mAnimator != null) {
            mAnimator.cancel();
        }
    }

    private void startRecord() {
        isRecording = true;
//        mCameraFacing.setVisibility(View.GONE);
        mVideoPath = getVideoFilePath(RecordVideoActivity.this);
        Log.e("path", mVideoPath);
        mCameraVideo.startRecordingVideo(mVideoPath);
//        mTitleView.setCompoundDrawablesWithIntrinsicBounds(
//                R.drawable.lv_timer_dot, 0, 0, 0);

//        if (mAnimator == null) {
//            mAnimator = ObjectAnimator.ofFloat(mTitleView, "alpha",
//                    1, 0.4f, 1)
//                    .setDuration(1000);
//
//            mAnimator.setRepeatCount(ObjectAnimator.INFINITE);
//            mAnimator.setRepeatMode(ObjectAnimator.REVERSE);
//        }
//        mAnimator.start();
    }


    private String getVideoFilePath(Context context) {
        final File dir = new File(context.getExternalFilesDir(null), "Videos");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return (dir.getAbsolutePath() + "/") + System.currentTimeMillis() + ".mp4";
    }

}
