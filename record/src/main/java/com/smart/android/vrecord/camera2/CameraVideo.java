package com.smart.android.vrecord.camera2;

import com.smart.android.vrecord.camera2.listener.OnCameraResultListener;
import com.smart.android.vrecord.camera2.listener.OnIsPreViewReadyListener;

/**
 * Created by Hyu on 2019-08-17.
 * Email: fvaryu@163.com
 */
public interface CameraVideo {

    void switchCameraFacing();

    public void setAutoFitTextureView(AutoFitTextureView textureView);

    void onPause();

    void onResume();

    void startPreview();


    void startRecordingVideo(String videoPath);

    void stopRecordingVideo();

    //暂停录制
    void pauseRecordVideo();

    //继续录制
    void resumeRecordVideo();

    void takePicture(String picPath);

//    void setOnRecordFinishListener(OnRecordFinishListener onRecordFinishListener);

    void setOnCameraResultListener(OnCameraResultListener resultListener);

    void setOnProgressChangeListener(OnProgressChangeListener onProgressChangeListener);


    void setOnIsPreViewReadyListener(OnIsPreViewReadyListener listener);

    interface OnProgressChangeListener {
        void onProgressChanged(int duration);
    }

    interface OnRecordFinishListener {
        void onRecordFinished();
    }

}
