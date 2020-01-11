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

    void startRecordingVideo(String videoPath);

    void stopRecordingVideo();

    void setOnProgressChangeListener(OnProgressChangeListener onProgressChangeListener);

    interface OnProgressChangeListener {
        void onProgressChanged(int duration);
    }
}
