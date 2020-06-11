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
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.smart.android.vrecord.camera2.OpenCameraInterface;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

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

    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CameraCaptureSession mCaptureSession;


    Handler mBackgroundHandler;
    private ImageReader mImageReader;


    private void setupImageReader(Size mPreviewSize) {

        //前三个参数分别是需要的尺寸和格式，最后一个参数代表每次最多获取几帧数据
        mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(), ImageFormat.JPEG, 2);

        //监听ImageReader的事件，当有图像流数据可用时会回调onImageAvailable方法，它的参数就是预览帧数据，可以对这帧数据进行处理
        mImageReader.setOnImageAvailableListener(reader -> {
            Image image = reader.acquireLatestImage();


        }, null);

    }


    public void takePicture(OpenCameraInterface openCameraInterface) {
        mCaptureSession = openCameraInterface.getmPreviewSession();
        mCameraDevice = openCameraInterface.getCameraDevice();
        mBackgroundHandler = openCameraInterface.getBackgroundHandler();
    }

    private void lockFocus(OpenCameraInterface openCameraInterface) {
        try {
            mPreviewRequestBuilder = openCameraInterface.getCameraDevice().createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
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
                if (afState == null) {
                    captureStillPicture();
                } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState
                        || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState
                        || CaptureResult.CONTROL_AF_STATE_INACTIVE == afState
                        || CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN == afState) {
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                        mPreviewState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
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
                    captureStillPicture();
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

    private void captureStillPicture() {
        try {
            if (null == mCameraDevice) {
                return;
            }
            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getPhotoOrientation(cameraConfigProvider.getSensorPosition()));

            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    Log.d("TAG", "onCaptureCompleted: ");
                }
            };

            mCaptureSession.stopRepeating();
            mCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);

        } catch (CameraAccessException e) {
            Log.e("TAG", "Error during capturing picture");
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
//            processCaptureResult(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
//            processCaptureResult(result);
        }

    };

    protected int getVideoOrientation( int sensorPosition) {
        final int degrees;
        switch (sensorPosition) {
            case 90:
                degrees = 0;
                break; // Natural orientation
            case 0:
                degrees = 90;
                break; // Landscape left
            case 270:
                degrees = 180;
                break;// Upside down
            case 180:
                degrees = 270;
                break;// Landscape right
            case -1:
            default:
                degrees = 0;
                break;
        }

        final int rotate;
//        if (Objects.equals(mCameraId, mFaceFrontCameraId)) {
//            rotate = (360 + mFaceFrontCameraOrientation + degrees) % 360;
//        } else {
//            rotate = (360 + mFaceBackCameraOrientation - degrees) % 360;
//        }
        return rotate;
    }


    @IntDef({STATE_PREVIEW, STATE_WAITING_LOCK, STATE_WAITING_PRE_CAPTURE, STATE_WAITING_NON_PRE_CAPTURE, STATE_PICTURE_TAKEN})
    @Retention(RetentionPolicy.SOURCE)
    @interface CameraPreviewState {
    }

}
