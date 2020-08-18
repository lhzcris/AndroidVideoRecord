package com.smart.android.vrecord.camera2.video;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;

import com.smart.android.vrecord.VideoRecordPicker;
import com.smart.android.vrecord.camera2.listener.OnCameraResultListener;
import com.smart.android.vrecord.camera2.listener.OnRecordInfoListener;
import com.smart.android.vrecord.camera2.OpenCameraInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class VideoRecorderManager {

//    private static final int MAX_COUNT = 30 * 1000;

    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();


    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
        DEFAULT_ORIENTATIONS.append(90, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    private Activity mActivity;
    private MediaRecorder mMediaRecorder;

    private Integer mSensorOrientation;

    private Size mVideoSize;

    private String mVideoPath;
    private CaptureRequest.Builder mPreviewBuilder;
    private int mOrientation;

//    private OnRecordInfoListener recordInfoListener;

    private OnCameraResultListener recordInfoListener;

    public VideoRecorderManager(Activity activity) {
        mActivity = activity;
    }

    public void setSensorOrientation(Integer sensorOrientation) {
        mSensorOrientation = sensorOrientation;
    }

    public void setVideoPath(String videoPath) {
        mVideoPath = videoPath;
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }


    public void setRecordInfoListener(OnCameraResultListener recordInfoListener) {
        this.recordInfoListener = recordInfoListener;
    }

    public void setVideoSize(Size videoSize) {
        mVideoSize = videoSize;
    }


    private void setUpMediaRecorder(OpenCameraInterface openCameraInterface) throws IOException {
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(mVideoPath);
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
//        mMediaRecorder.setPreviewDisplay(openCameraInterface.getTextureView().getSurfaceTexture());
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        if (VideoRecordPicker.getInstance().getMaxDuration() > 1000) {
            mMediaRecorder.setMaxDuration((int) VideoRecordPicker.getInstance().getMaxDuration());//录制最大时长 ms
            mMediaRecorder.setOnInfoListener((mr, what, extra) -> {
                Log.e("sss", what + "");
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    Log.e("sss", "完成录制");
                    stopRecordingVideo();
                    if (recordInfoListener != null) {
                        recordInfoListener.onVideoRecorded(mVideoPath);
                    }
                }
            });
        }
//        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        Log.e("rotation", mSensorOrientation + "," + mOrientation);
        switch (mSensorOrientation) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(mOrientation));
                break;
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(mOrientation));
                break;
        }
        mMediaRecorder.prepare();
    }


    public void startRecordingVideo(OpenCameraInterface openCameraInterface, TextureView textureView) {
        try {
            openCameraInterface.closePreviewSession();
            setUpMediaRecorder(openCameraInterface);
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(openCameraInterface.getPreviewSize().getWidth(),
                    openCameraInterface.getPreviewSize().getHeight());
            mPreviewBuilder = openCameraInterface.getCameraDevice()
                    .createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();

            // Set up Surface for the camera preview
            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);

            // Set up Surface for the MediaRecorder
            Surface recorderSurface = mMediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            mPreviewBuilder.addTarget(recorderSurface);

            // Start a capture session
            // Once the session starts, we can update the UI and start recording
            openCameraInterface.getCameraDevice()
                    .createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            openCameraInterface.updatePreview(mPreviewBuilder, cameraCaptureSession);
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Start recording
                                    mMediaRecorder.start();
                                }
                            });
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                        }
                    }, openCameraInterface.getBackgroundHandler());
        } catch (CameraAccessException | IOException e) {
            e.printStackTrace();
        }

    }

    public void pause() {
        if (mMediaRecorder != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mMediaRecorder.pause();
            }
        }
    }

    public void resume() {
        if (mMediaRecorder != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mMediaRecorder.resume();
            }
        }
    }


    public void stopRecordingVideo() {
        // UI
        // Stop recording
        if (mMediaRecorder == null)
            return;
        try {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        } catch (Exception e) {
            e.printStackTrace();
//            throw new RuntimeException("已经停止了");
            mMediaRecorder=null;
        }


    }

}
