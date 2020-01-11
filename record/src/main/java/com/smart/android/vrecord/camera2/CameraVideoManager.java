package com.smart.android.vrecord.camera2;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.view.OrientationEventListener;
import android.view.TextureView;

import androidx.annotation.NonNull;

import com.smart.android.vrecord.camera2.video.VideoRecorderManager;

/**
 * Created by Hyu on 2019-08-17.
 * Email: fvaryu@163.com
 */
public final class CameraVideoManager implements
        TextureView.SurfaceTextureListener, CameraVideo {
    private static final int DONE = 102;
    private static final int START = 100;
    private static final int TIMER = 101;
    private AlbumOrientationEventListener mAlbumOrientationEventListener;

    private Context mContext;
    private AutoFitTextureView mAutoFitTextureView;
    private OpenCameraInterface mOpenCameraInterface;
    private VideoRecorderManager mVideoRecorderManager;
    private OnProgressChangeListener mOnProgressChangeListener;

    private CameraFacing mCameraFacing = CameraFacing.BACK;
    private Handler mHandler;
    private int mOrientation;

    public CameraVideoManager(Context context) {
        this(new OpenCameraInterface((Activity) context));
        this.mContext = context;
        mAlbumOrientationEventListener = new AlbumOrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public CameraVideoManager(OpenCameraInterface openCameraInterface) {
        mOpenCameraInterface = openCameraInterface;
    }

    private class AlbumOrientationEventListener extends OrientationEventListener {
        public AlbumOrientationEventListener(Context context) {
            super(context);
        }

        public AlbumOrientationEventListener(Context context, int rate) {
            super(context, rate);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                return;
            }

            //保证只返回四个方向
            int newOrientation = ((orientation + 45) / 90 * 90) % 360;
            if (newOrientation != mOrientation) {
                mOrientation = newOrientation;

                //返回的mOrientation就是手机方向，为0°、90°、180°和270°中的一个
            }
        }
    }

    @Override
    public void switchCameraFacing() {
        if (mCameraFacing == CameraFacing.BACK) {
            mCameraFacing = CameraFacing.FRONT;
        } else {
            mCameraFacing = CameraFacing.BACK;
        }

        onPause();
        onResume();
    }

    public void setAutoFitTextureView(AutoFitTextureView textureView) {
        if (mOpenCameraInterface != null) {
            mOpenCameraInterface.setTextureView(textureView);
        }
        this.mAutoFitTextureView = textureView;
    }

    // ***************SurfaceTextureListener****************** //
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        mOpenCameraInterface.openCamera(mCameraFacing.ordinal(), width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        mOpenCameraInterface.configureTransform(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    @Override
    public void onPause() {
        mOpenCameraInterface.closeCamera();
        stopRecordingVideo();
        mOpenCameraInterface.stopBackgroundThread();
        mAlbumOrientationEventListener.disable();
    }

    @Override
    public void onResume() {
        if (mAlbumOrientationEventListener.canDetectOrientation()) {
            mAlbumOrientationEventListener.enable();
        }
        mOpenCameraInterface.startBackgroundThread();
        if (mAutoFitTextureView.isAvailable()) {
            mOpenCameraInterface.openCamera(mCameraFacing.ordinal(),
                    mAutoFitTextureView.getWidth(), mAutoFitTextureView.getHeight());
        } else {
            mAutoFitTextureView.setSurfaceTextureListener(this);
        }
    }

    @Override
    public void startRecordingVideo(String videoPath) {
        if (mVideoRecorderManager == null) {
            mVideoRecorderManager = new VideoRecorderManager((Activity) mContext);
        }
        mVideoRecorderManager.setOrientation(mOrientation);
        mVideoRecorderManager.setVideoPath(videoPath);
        mVideoRecorderManager.setVideoSize(mOpenCameraInterface.getVideoSize());
        mVideoRecorderManager.setSensorOrientation(mOpenCameraInterface.getSensorOrientation());
        mVideoRecorderManager.startRecordingVideo(mOpenCameraInterface, mAutoFitTextureView);
        if (mHandler != null)
            mHandler.sendEmptyMessage(START);
    }

    @Override
    public void stopRecordingVideo() {
        if (mVideoRecorderManager != null) {
            mVideoRecorderManager.stopRecordingVideo();
            mOpenCameraInterface.startPreview();

            if (mHandler != null) {
                mHandler.removeMessages(TIMER);
                mHandler.sendEmptyMessage(DONE);
                mVideoRecorderManager = null;
            }
        }
    }

    @Override
    public void setOnProgressChangeListener(OnProgressChangeListener onProgressChangeListener) {
        this.mOnProgressChangeListener = onProgressChangeListener;
        if (mOnProgressChangeListener != null) {
            mHandler = new Handler(new Handler.Callback() {
                int duration = 0;

                @Override
                public boolean handleMessage(@NonNull Message message) {
                    switch (message.what) {
                        case START: {
                            mHandler.sendEmptyMessageDelayed(TIMER, 1000);
                        }
                        break;
                        case TIMER: {
                            duration += 1;
                            mHandler.sendEmptyMessageDelayed(TIMER, 1000);
                        }
                        break;
                        case DONE: {
                            duration = 0;
                        }
                        break;
                    }
                    if (message.what != DONE) {
                        mOnProgressChangeListener.onProgressChanged(duration);
                    }
                    return true;
                }
            });
        }
    }
}
