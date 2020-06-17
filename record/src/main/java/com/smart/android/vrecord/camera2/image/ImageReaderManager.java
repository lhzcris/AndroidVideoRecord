package com.smart.android.vrecord.camera2.image;

import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.smart.android.utils.Logger;
import com.smart.android.vrecord.camera2.listener.OnCameraResultListener;
import com.smart.android.vrecord.camera2.OpenCameraInterface;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author liuhuazhong
 * @since 20200119
 */
public class ImageReaderManager {


    private static final SparseIntArray ORIENTATION = new SparseIntArray();

    static {
        ORIENTATION.append(Surface.ROTATION_0, 90);
        ORIENTATION.append(Surface.ROTATION_90, 0);
        ORIENTATION.append(Surface.ROTATION_180, 270);
        ORIENTATION.append(Surface.ROTATION_270, 180);
    }

    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAITING_LOCK = 1;
    private static final int STATE_WAITING_PRE_CAPTURE = 2;
    private static final int STATE_WAITING_NON_PRE_CAPTURE = 3;
    private static final int STATE_PICTURE_TAKEN = 4;

    @CameraPreviewState
    private int mPreviewState = STATE_PREVIEW;

    private OnCameraResultListener resultListener;

    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CameraCaptureSession mCaptureSession;


    private Integer mSensorOrientation;

    Handler mBackgroundHandler;
    private ImageReader mImageReader;

    private String mOutputPath;

    private Handler UIhandler = new Handler(Looper.getMainLooper());

    public void setupImageReader(Size mPreviewSize) {

        //前三个参数分别是需要的尺寸和格式，最后一个参数代表每次最多获取几帧数据
        mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(), ImageFormat.JPEG, 2);

        //监听ImageReader的事件，当有图像流数据可用时会回调onImageAvailable方法，它的参数就是预览帧数据，可以对这帧数据进行处理
        mImageReader.setOnImageAvailableListener(reader -> {

            Image image = reader.acquireNextImage();

            mBackgroundHandler.post(new ImageSaver(image, new File(mOutputPath), new ImageSaver.ImageSaverCallback() {
                @Override
                public void onSuccessFinish(byte[] bytes) {
                    Logger.e("mOutputPath==" + mOutputPath);
                    UIhandler.post(() -> {
//                        if (VideoRecordPicker.getInstance().getFinishListener() != null) {
//                            VideoRecordPicker.getInstance().getFinishListener().onFinishListener(mOutputPath);
//                        }
                        if (resultListener != null)
                            resultListener.onPhotoTaken(bytes, mOutputPath);
                    });
                    unlockFocus();
                }

                @Override
                public void onError() {
                    Log.e("onError", mOutputPath);
                }
            }));


        }, mBackgroundHandler);

    }


    public void takePicture(OpenCameraInterface openCameraInterface, TextureView textureView) {
        mCaptureSession = openCameraInterface.getmPreviewSession();
        mCameraDevice = openCameraInterface.getCameraDevice();
        mBackgroundHandler = openCameraInterface.getBackgroundHandler();

        mBackgroundHandler.post(() -> {
            lockFocus(openCameraInterface);
        });
    }

    private void capture() {
        if (mCameraDevice == null) return;
        try {
            //首先我们创建请求拍照的CaptureRequest
            final CaptureRequest.Builder mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

            mCaptureBuilder.addTarget(mImageReader.getSurface());

            //使用相同的AE和AF模式作为预览。
            mCaptureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            //设置拍照方向
            mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATION.get(mSensorOrientation));

            //停止预览
            mCaptureSession.stopRepeating();

            //开始拍照，然后回调上面的接口重启预览，因为mCaptureBuilder设置ImageReader作为target，所以会自动回调ImageReader的onImageAvailable()方法保存图片
            CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    Logger.e("onCaptureCompleted:");
                }
            };

            mCaptureSession.capture(mCaptureBuilder.build(), captureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Logger.e("Error during capturing picture");
        }
    }


    private void lockFocus(OpenCameraInterface openCameraInterface) {
        try {
            mPreviewRequestBuilder = openCameraInterface.getPreviewBuilder();

            //对焦
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);

            mPreviewState = STATE_WAITING_LOCK;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), captureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private void processCaptureResult(CaptureResult result) {
        switch (mPreviewState) {
            case STATE_PREVIEW: {
                break;
            }
            case STATE_WAITING_LOCK: {
                final Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                Log.e("afState", "afState=" + afState);
                if (afState == null) {
                    capture();
                } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState
                        || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState
//                        || CaptureResult.CONTROL_AF_STATE_INACTIVE == afState
                        || CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN == afState) {
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            (CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN != afState
                                    && aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED)//todo 自动对焦问题
//                            aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED
                    ) {
                        mPreviewState = STATE_PICTURE_TAKEN;
                        capture();
                    } else {
                        runPreCaptureSequence();
                    }
                }
                break;
            }
            case STATE_WAITING_PRE_CAPTURE: {
                final Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                if (aeState == null ||
                        aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                        aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                    mPreviewState = STATE_WAITING_NON_PRE_CAPTURE;
                }
                break;
            }
            case STATE_WAITING_NON_PRE_CAPTURE: {
                final Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                    mPreviewState = STATE_PICTURE_TAKEN;
                    capture();
                }
                break;
            }
            case STATE_PICTURE_TAKEN:
                break;
        }
    }

    private void runPreCaptureSequence() {
        try {
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            mPreviewState = STATE_WAITING_PRE_CAPTURE;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), captureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
        }
    }


    private void unlockFocus() {
        try {
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), captureCallback, mBackgroundHandler);
            mPreviewState = STATE_PREVIEW;
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), captureCallback, mBackgroundHandler);
        } catch (Exception e) {
            Log.e("TAG", "Error during focus unlocking");
        }
    }


    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            processCaptureResult(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            processCaptureResult(result);
        }

    };


    public void setSensorOrientation(Integer mSensorOrientation) {
        this.mSensorOrientation = mSensorOrientation;
    }

    public void setmOutputPath(String mOutputPath) {
        this.mOutputPath = mOutputPath;
    }

    public ImageReader getmImageReader() {
        return mImageReader;
    }

    public void setResultListener(OnCameraResultListener resultListener) {
        this.resultListener = resultListener;
    }

    public void closeImageReader() {
        if (null != mImageReader) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    @IntDef({STATE_PREVIEW, STATE_WAITING_LOCK, STATE_WAITING_PRE_CAPTURE, STATE_WAITING_NON_PRE_CAPTURE, STATE_PICTURE_TAKEN})
    @Retention(RetentionPolicy.SOURCE)
    @interface CameraPreviewState {
    }

}
