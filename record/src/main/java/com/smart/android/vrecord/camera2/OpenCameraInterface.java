package com.smart.android.vrecord.camera2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.smart.android.utils.DisplayUtil;
import com.smart.android.utils.Logger;
import com.smart.android.vrecord.camera2.image.ImageReaderManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class OpenCameraInterface extends CameraDevice.StateCallback {
    private static final String TAG = OpenCameraInterface.class.getName();

    private Activity mContext;
    private Integer mSensorOrientation;
    private Size mVideoSize;
    private Size mPreviewSize;
    private CameraDevice mCameraDevice;
    private AutoFitTextureView mTextureView;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private CameraCaptureSession mPreviewSession;
    private CaptureRequest.Builder mPreviewBuilder;

    private ImageReader imageReader;

    private boolean isPreViewReady;

    OpenCameraInterface(Activity context) {
        mContext = context;
    }

    @SuppressLint("MissingPermission")
    void openCamera(int cameraId, ImageReaderManager imageReaderManager) {

        CameraManager cameraManager = (CameraManager) mContext.getApplicationContext()
                .getSystemService(Context.CAMERA_SERVICE);

        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            if (ActivityCompat.checkSelfPermission(mContext,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(String.valueOf(cameraId));
            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {
                return;
            }

            int screenHeight = DisplayUtil.getScreenHeight(mContext);
            int screenWidth = DisplayUtil.getScreenWidth(mContext);
            mSensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            mVideoSize = CameraSizeUtils.chooseMeadiaSzie(map.getOutputSizes(MediaRecorder.class), screenHeight, screenWidth);
            mPreviewSize = CameraSizeUtils.chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), mVideoSize);

            if (imageReaderManager != null) {
                imageReaderManager.setupImageReader(mPreviewSize);
            }
            imageReader = imageReaderManager.getmImageReader();

            Logger.e("preview width=" + mPreviewSize.getWidth() +
                    ",height=" + mPreviewSize.getHeight() +
                    ", video size width=" + mVideoSize.getWidth() +
                    ", height=" + mVideoSize.getHeight());

            int orientation = mContext.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            } else {
                mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
            }
            //handler 确定Callback在哪个线程执行，为null的话就在当前线程执行
            cameraManager.openCamera(String.valueOf(cameraId), this, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    public boolean isClose() {
        return mCameraDevice == null;
    }

    public boolean isPreViewReady() {
        return isPreViewReady;
    }

    void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            closePreviewSession();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }


    void startPreview() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            closePreviewSession();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            //设置TextureView的缓冲区大小
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //获取Surface显示预览数据
            Surface previewSurface = new Surface(texture);
            mPreviewBuilder.addTarget(previewSurface);

            Surface imageReaderSurface = imageReader.getSurface();

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, imageReaderSurface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mPreviewSession = session;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        }
                    }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    void updatePreview() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(mPreviewBuilder);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                    super.onCaptureFailed(session, request, failure);
                    isPreViewReady = false;
                }

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    updatePreViewReady(result);
                }

                @Override
                public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
                    super.onCaptureProgressed(session, request, partialResult);

                    updatePreViewReady(partialResult);
                }
            }, mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /** 预览聚焦成功 方可拍照 */
    private void updatePreViewReady(CaptureResult result) {
        final Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
//                    Log.e("afState", "afState=" + afState);
        if (afState != null && afState == CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED) {
//                        Logger.e("update,onCaptureProgressed=" + afState);
            isPreViewReady = true;
        }

    }

    public void updatePreview(CaptureRequest.Builder builder, CameraCaptureSession session) {
        this.mPreviewBuilder = builder;
        this.mPreviewSession = session;
        updatePreview();
    }

    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        // 自动对焦应
        builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
    }


    public void closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
    }

    void configureTransform(int viewWidth, int viewHeight) {
        if (null == mTextureView || null == mPreviewSize) {
            return;
        }
        int rotation = mContext.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    public void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onOpened(@NonNull CameraDevice cameraDevice) {
        mCameraDevice = cameraDevice;
        startPreview();
        mCameraOpenCloseLock.release();
        if (null != mTextureView) {
            configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
        }
    }

    @Override
    public void onDisconnected(@NonNull CameraDevice cameraDevice) {
        mCameraOpenCloseLock.release();
        cameraDevice.close();
        mCameraDevice = null;

    }

    @Override
    public void onError(@NonNull CameraDevice cameraDevice, int i) {
        mCameraOpenCloseLock.release();
        cameraDevice.close();
        mCameraDevice = null;
    }

    public Size getPreviewSize() {
        return mPreviewSize;
    }

    public void setPreviewSize(Size previewSize) {
        mPreviewSize = previewSize;
    }

    public CameraDevice getCameraDevice() {
        return mCameraDevice;
    }

    public void setCameraDevice(CameraDevice cameraDevice) {
        mCameraDevice = cameraDevice;
    }

    public CameraCaptureSession getmPreviewSession() {
        return mPreviewSession;
    }

    public AutoFitTextureView getTextureView() {
        return mTextureView;
    }

    public void setTextureView(AutoFitTextureView textureView) {
        mTextureView = textureView;
    }

    public HandlerThread getBackgroundThread() {
        return mBackgroundThread;
    }

    public void setBackgroundThread(HandlerThread backgroundThread) {
        mBackgroundThread = backgroundThread;
    }

    public Handler getBackgroundHandler() {
        return mBackgroundHandler;
    }

    public void setBackgroundHandler(Handler backgroundHandler) {
        mBackgroundHandler = backgroundHandler;
    }

    public Integer getSensorOrientation() {
        return mSensorOrientation;
    }

    public void setSensorOrientation(Integer sensorOrientation) {
        mSensorOrientation = sensorOrientation;
    }

    public Size getVideoSize() {
        return mVideoSize;
    }

    public void setVideoSize(Size videoSize) {
        mVideoSize = videoSize;
    }

    public CaptureRequest.Builder getPreviewBuilder() {
        return mPreviewBuilder;
    }
}
