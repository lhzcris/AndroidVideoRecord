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
                    Log.e("success", mOutputPath);
                    UIhandler.post(() -> {
//                        if (VideoRecordPicker.getInstance().getFinishListener() != null) {
//                            VideoRecordPicker.getInstance().getFinishListener().onFinishListener(mOutputPath);
//                        }
                        if (resultListener!=null)
                            resultListener.onPhotoTaken(bytes,mOutputPath);
                    });
                    unlockFocus();
                }

                @Override
                public void onError() {
                    Log.e("onError", mOutputPath);
                }
            }));


        }, null);

    }


//    OpenCameraInterface openCameraInterface;
//    TextureView textureView;

    public void takePicture(OpenCameraInterface openCameraInterface, TextureView textureView) {
        mCaptureSession = openCameraInterface.getmPreviewSession();
        mCameraDevice = openCameraInterface.getCameraDevice();
        mBackgroundHandler = openCameraInterface.getBackgroundHandler();

//        this.openCameraInterface = openCameraInterface;
//        this.textureView = textureView;

//        capture(openCameraInterface, textureView);
        mBackgroundHandler.post(() -> {
            lockFocus(openCameraInterface);
        });
    }

    private void capture() {//OpenCameraInterface openCameraInterface, TextureView textureView
        if (mCameraDevice == null) return;
        try {
            //首先我们创建请求拍照的CaptureRequest
            final CaptureRequest.Builder mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

//            SurfaceTexture texture = textureView.getSurfaceTexture();
//            assert texture != null;
//            texture.setDefaultBufferSize(openCameraInterface.getPreviewSize().getWidth(),
//                    openCameraInterface.getPreviewSize().getHeight());
//            Surface previewSurface = new Surface(texture);
//            mCaptureBuilder.addTarget(previewSurface);


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

                }
            };

            mCaptureSession.capture(mCaptureBuilder.build(), captureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private void lockFocus(OpenCameraInterface openCameraInterface) {
        try {
            mPreviewRequestBuilder = openCameraInterface.getPreviewBuilder();

            //对焦
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);

            mPreviewState = STATE_WAITING_LOCK;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), captureCallback, null);
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
                    capture();
                } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState
                        || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState
                        || CaptureResult.CONTROL_AF_STATE_INACTIVE == afState
                        || CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN == afState) {
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                        mPreviewState = STATE_PICTURE_TAKEN;
//                        captureStillPicture();
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
//                    captureStillPicture();
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
            mCaptureSession.capture(mPreviewRequestBuilder.build(), captureCallback, null);
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

//    protected int getVideoOrientation( int sensorPosition) {
//        final int degrees;
//        switch (sensorPosition) {
//            case 90:
//                degrees = 0;
//                break; // Natural orientation
//            case 0:
//                degrees = 90;
//                break; // Landscape left
//            case 270:
//                degrees = 180;
//                break;// Upside down
//            case 180:
//                degrees = 270;
//                break;// Landscape right
//            case -1:
//            default:
//                degrees = 0;
//                break;
//        }
//
//        final int rotate;
////        if (Objects.equals(mCameraId, mFaceFrontCameraId)) {
////            rotate = (360 + mFaceFrontCameraOrientation + degrees) % 360;
////        } else {
////            rotate = (360 + mFaceBackCameraOrientation - degrees) % 360;
////        }
//        return rotate;
//    }


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
