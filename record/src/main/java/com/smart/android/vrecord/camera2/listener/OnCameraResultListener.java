package com.smart.android.vrecord.camera2.listener;

/**
 * @author liuhuazhong
 * @since 20200612
 */
public interface OnCameraResultListener {

    //Called when the video record is finished and saved
    void onVideoRecorded(String filePath);

    //called when the photo is taken and saved
    void onPhotoTaken(byte[] bytes, String filePath);
}
