package com.smart.android.vrecord.camera2

import android.app.Activity
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.SensorManager
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.OrientationEventListener
import android.view.TextureView.SurfaceTextureListener
import com.smart.android.utils.Logger
import com.smart.android.vrecord.camera2.CameraVideo.OnProgressChangeListener
import com.smart.android.vrecord.camera2.image.ImageReaderManager
import com.smart.android.vrecord.camera2.listener.OnCameraResultListener
import com.smart.android.vrecord.camera2.video.VideoRecorderManager


/**
 * Created by Hyu on 2019-08-17.
 * Email: fvaryu@163.com
 */
class CameraVideoManager(private val mOpenCameraInterface: OpenCameraInterface) :
    SurfaceTextureListener, CameraVideo {
    private var mAlbumOrientationEventListener: AlbumOrientationEventListener? =
        null
    private var mContext: Context? = null
    private var mAutoFitTextureView: AutoFitTextureView? = null
    private var mVideoRecorderManager: VideoRecorderManager? = null
    private var mImageReaderManager: ImageReaderManager? = null
    private var mOnProgressChangeListener: OnProgressChangeListener? = null
    private var onCameraResultListener: OnCameraResultListener? = null
    private var mCameraFacing = CameraFacing.BACK
    private var mHandler: Handler? = null
    private var mOrientation = 0


    constructor(context: Context?) : this(OpenCameraInterface(context as Activity?)) {
        mContext = context
        mAlbumOrientationEventListener =
            AlbumOrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private inner class AlbumOrientationEventListener : OrientationEventListener {
        constructor(context: Context?) : super(context) {}
        constructor(context: Context?, rate: Int) : super(context, rate) {}

        override fun onOrientationChanged(orientation: Int) {
            if (orientation == ORIENTATION_UNKNOWN) {
                return
            }
            //保证只返回四个方向
            val newOrientation = (orientation + 45) / 90 * 90 % 360
            if (newOrientation != mOrientation) {
                mOrientation = newOrientation
                //返回的mOrientation就是手机方向，为0°、90°、180°和270°中的一个
            }
        }
    }

    override fun switchCameraFacing() {
        if (mOpenCameraInterface!!.isClose) return
        mCameraFacing = if (mCameraFacing == CameraFacing.BACK) {
            CameraFacing.FRONT
        } else {
            CameraFacing.BACK
        }
        onPause()
        onResume()
    }

    override fun setAutoFitTextureView(textureView: AutoFitTextureView) {
        if (mOpenCameraInterface != null) {
            mOpenCameraInterface.textureView = textureView
        }
        mAutoFitTextureView = textureView
    }

    override fun setOnCameraResultListener(resultListener: OnCameraResultListener) {
        this.onCameraResultListener = resultListener
    }

    // ***************SurfaceTextureListener****************** //
    override fun onSurfaceTextureAvailable(
        surfaceTexture: SurfaceTexture,
        width: Int,
        height: Int
    ) {
        if (mImageReaderManager == null)
            mImageReaderManager = ImageReaderManager()
        mOpenCameraInterface!!.openCamera(mCameraFacing.ordinal, mImageReaderManager)
    }

    override fun onSurfaceTextureSizeChanged(
        surfaceTexture: SurfaceTexture,
        width: Int,
        height: Int
    ) {
        mOpenCameraInterface!!.configureTransform(width, height)
    }

    override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
        return false
    }

    override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}

    override fun onPause() {
        mOpenCameraInterface!!.closeCamera()
        stopRecordingVideo()
        mImageReaderManager?.closeImageReader()
        mImageReaderManager = null
        mOpenCameraInterface.stopBackgroundThread()
        mAlbumOrientationEventListener!!.disable()
        mHandler?.removeMessages(TIMER)
    }

    override fun onResume() {
        if (mImageReaderManager == null) {
            mImageReaderManager = ImageReaderManager()
        }
        if (mAlbumOrientationEventListener!!.canDetectOrientation()) {
            mAlbumOrientationEventListener!!.enable()
        }
        mOpenCameraInterface!!.startBackgroundThread()
        if (mAutoFitTextureView!!.isAvailable) {
            mOpenCameraInterface.openCamera(mCameraFacing.ordinal, mImageReaderManager)
        } else {
            mAutoFitTextureView!!.surfaceTextureListener = this
        }
    }

    override fun pauseRecordVideo() {
        if (mVideoRecorderManager != null) mVideoRecorderManager!!.pause()
        mHandler?.removeMessages(TIMER)
    }

    override fun resumeRecordVideo() {
        if (mVideoRecorderManager != null) mVideoRecorderManager!!.resume()
        if (mHandler != null) mHandler!!.sendEmptyMessage(START)
    }

    override fun startRecordingVideo(videoPath: String) {
        if (mVideoRecorderManager == null) {
            mVideoRecorderManager = VideoRecorderManager(mContext as Activity?)
        }

        mVideoRecorderManager?.apply {
            setOrientation(mOrientation)
            setVideoPath(videoPath)
            setVideoSize(mOpenCameraInterface.videoSize)
            setSensorOrientation(mOpenCameraInterface.sensorOrientation)
            setRecordInfoListener(onCameraResultListener)
            startRecordingVideo(mOpenCameraInterface, mAutoFitTextureView)
        }
//        mVideoRecorderManager!!.setRecordInfoListener { if (mOnRecordFinishListener != null) mOnRecordFinishListener!!.onRecordFinished() }
        if (mHandler != null) mHandler!!.sendEmptyMessage(START)
    }

    /**拍照*/
    override fun takePicture(outPath: String) {

        if (mOpenCameraInterface.isClose || mOpenCameraInterface.isPreViewReady.not())
            return
//        Log.e("takee","take pic ${mOpenCameraInterface.isPreViewReady}")

        if (mImageReaderManager == null) {
            mImageReaderManager = ImageReaderManager()
        }
        mImageReaderManager!!.apply {
            setSensorOrientation(mOrientation)
            setmOutputPath(outPath)
//            setupImageReader(mOpenCameraInterface.previewSize)
            setResultListener(onCameraResultListener)
            takePicture(mOpenCameraInterface)
        }
    }

    override fun startPreview() {
        mOpenCameraInterface.startPreview()
    }

    override fun stopRecordingVideo() {
        if (mVideoRecorderManager != null) { //            mOpenCameraInterface.startPreview();
            mVideoRecorderManager!!.stopRecordingVideo()
            if (mHandler != null) {
                mHandler!!.removeMessages(TIMER)
                mHandler!!.sendEmptyMessage(DONE)
                mVideoRecorderManager = null
            }
        }
    }

    override fun setOnProgressChangeListener(onProgressChangeListener: OnProgressChangeListener) {
        mOnProgressChangeListener = onProgressChangeListener
        if (mOnProgressChangeListener != null) {
            mHandler = Handler(object : Handler.Callback {
                var duration = 0
                override fun handleMessage(message: Message): Boolean {
                    when (message.what) {
                        START -> {
                            mHandler!!.sendEmptyMessageDelayed(
                                TIMER,
                                1000
                            )
                        }
                        TIMER -> {
                            duration += 1
                            mHandler!!.sendEmptyMessageDelayed(
                                TIMER,
                                1000
                            )
                        }
                        DONE -> {
                            duration = 0
                        }
                    }
                    if (message.what != DONE) {
                        mOnProgressChangeListener!!.onProgressChanged(duration)
                    }
                    return true
                }
            })
        }
    }


    companion object {
        private const val DONE = 102
        private const val START = 100
        private const val TIMER = 101
    }

}