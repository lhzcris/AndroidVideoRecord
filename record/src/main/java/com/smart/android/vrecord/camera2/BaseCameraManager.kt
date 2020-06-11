package com.smart.android.vrecord.camera2

import android.app.Activity
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.SensorManager
import android.view.OrientationEventListener
import android.view.TextureView.SurfaceTextureListener

/**
 * @author liuhuazhong
 * @since 20200611
 */
class BaseCameraManager (private val mOpenCameraInterface: OpenCameraInterface?)  : SurfaceTextureListener, CameraVideo {


    constructor(context: Context?) : this(OpenCameraInterface(context as Activity?)) {
        mContext = context
        mAlbumOrientationEventListener =
            AlbumOrientationEventListener(
                context,
                SensorManager.SENSOR_DELAY_NORMAL
            )
    }

    private var mContext: Context? = null
    private var mAlbumOrientationEventListener: AlbumOrientationEventListener? = null
    private var mCameraFacing: CameraFacing? = CameraFacing.BACK
    private var mOrientation = 0

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

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setOnRecordFinishListener(onRecordFinishListener: CameraVideo.OnRecordFinishListener?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onResume() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setAutoFitTextureView(textureView: AutoFitTextureView?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPause() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun resumeRecordVideo() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun startRecordingVideo(videoPath: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setOnProgressChangeListener(onProgressChangeListener: CameraVideo.OnProgressChangeListener?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun switchCameraFacing() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun startPreview() {
        mOpenCameraInterface?.startPreview()
    }

    override fun pauseRecordVideo() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun stopRecordingVideo() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}