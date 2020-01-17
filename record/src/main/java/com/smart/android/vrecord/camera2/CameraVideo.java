package com.smart.android.vrecord.camera2;

/**
 * Created by Hyu on 2019-08-17.
 * Email: fvaryu@163.com
 */
public interface CameraVideo {

    void switchCameraFacing();

    void setAutoFitTextureView(AutoFitTextureView textureView);

    void onPause();

    void onResume();

    void startPreview();

    void startRecordingVideo(String videoPath);

    void stopRecordingVideo();

    //暂停录制
    void pauseRecordVideo();

    //继续录制
    void resumeRecordVideo();

    void setOnRecordFinishListener(OnRecordFinishListener onRecordFinishListener);

    void setOnProgressChangeListener(OnProgressChangeListener onProgressChangeListener);

    interface OnProgressChangeListener {
        void onProgressChanged(int duration);
    }

    interface OnRecordFinishListener {
        void onRecordFinished();
    }
}
