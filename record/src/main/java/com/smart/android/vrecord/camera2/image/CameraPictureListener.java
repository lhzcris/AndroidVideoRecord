package com.smart.android.vrecord.camera2.image;

import com.smart.android.vrecord.camera2.listener.OnCameraResultListener;

import java.io.File;

/**
 * @author liuhuazhong
 * @since 20200612
 */
public interface CameraPictureListener {

    void onPictureTaken(byte[] bytes, File photoFile, OnCameraResultListener callback);

    void onPictureTakeError();
}
