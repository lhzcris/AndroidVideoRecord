package com.smart.android.vrecord.xwrecord

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
import android.media.MediaPlayer.OnVideoSizeChangedListener
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.VideoView
import com.gyf.immersionbar.ImmersionBar
import com.smart.android.image.ImageLoader
import com.smart.android.utils.Logger
import com.smart.android.vrecord.R
import com.smart.android.vrecord.camera2.AutoFitTextureView
import com.smart.android.vrecord.camera2.CameraVideo
import com.smart.android.vrecord.camera2.CameraVideoManager
import com.smart.android.vrecord.camera2.listener.OnCameraResultListener
import com.smart.android.vrecord.listener.*
import com.smart.android.vrecord.ui.PreViewOnChanagerListener
import com.smart.android.vrecord.widget.CaptureLayout
import com.smart.android.vrecord.widget.FoucsView
import java.io.File
import java.io.IOException

/**
 */
class JCameraView @JvmOverloads constructor(
    private val mContext: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(mContext, attrs, defStyleAttr) {

    companion object {
        //拍照浏览时候的类型
        const val TYPE_PICTURE = 0x001
        const val TYPE_VIDEO = 0x002
        const val TYPE_SHORT = 0x003
        const val TYPE_DEFAULT = 0x004
    }


    //回调监听
    private var jCameraLisenter: JCameraListener? = null
    private var leftClickListener: ClickListener? = null
    private var rightClickListener: ClickListener? = null
    private var mAutoTextView: AutoFitTextureView? = null
    private var mVideoView: VideoView? = null
    private var mPhoto: ImageView? = null
    private var mSwitchCamera: ImageView? = null
    private var mCaptureLayout: CaptureLayout? = null
    private var mMediaPlayer: MediaPlayer? = null

    //捕获的图片
    private val captureBitmap: Bitmap? = null
    private var firstFrame: Bitmap? = null//第一帧图片:

    //视频URL
    private var videoUrl: String = ""

    private var picUrl = ""

    //切换摄像头按钮的参数
    private var iconSize = 0 //图标大小
    private var iconMargin = 0 //右上边距
    private var iconSrc = 0 //图标资源
    private var iconLeft = 0 //左图标
    private var iconRight = 0 //右图标
    private var duration = 0 //录制时间

    //缩放梯度
    private var zoomGradient = 0
    private var firstTouch = true
    private var firstTouchLength = 0f

    //bar高度
    var barHeight: Int = 0


    fun setMBarHeight(value: Int) {
        mSwitchCamera?.run {
            val lp: LinearLayout.LayoutParams = layoutParams as LinearLayout.LayoutParams
            lp.setMargins(lp.leftMargin, value + lp.topMargin, lp.rightMargin, lp.bottomMargin)
            layoutParams = lp

        }
    }


    var mLastClickTime = 0L
    val TIME_INTERVAL = 800L

    /**权限 */
    private var isPermission = true

    /**
     * act不在被覆盖时 是否执行 mCameraVideo.onPause()；
     */
    private var is2Pause = false
    private var mCameraVideo: CameraVideo? = null

    fun isHasPermission() = isPermission

    init {
        val a =
            mContext.theme.obtainStyledAttributes(attrs, R.styleable.JCameraView, defStyleAttr, 0)
        iconSize = a.getDimensionPixelSize(
            R.styleable.JCameraView_iconSize, TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 35f, resources.displayMetrics
            ).toInt()
        )
        iconMargin = a.getDimensionPixelSize(
            R.styleable.JCameraView_iconMargin, TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 15f, resources.displayMetrics
            ).toInt()
        )
        iconSrc = a.getResourceId(R.styleable.JCameraView_iconSrc, R.drawable.ic_camera)
        iconLeft = a.getResourceId(R.styleable.JCameraView_iconLeft, 0)
        iconRight = a.getResourceId(R.styleable.JCameraView_iconRight, 0)
        duration = a.getInteger(R.styleable.JCameraView_duration_max, 10 * 1000) //没设置默认为10s
        a.recycle()
        initView()
    }

    private fun initView() {
        setWillNotDraw(false)
        val view = LayoutInflater.from(mContext).inflate(R.layout.camera_view, this)
        mAutoTextView = view.findViewById(R.id.autoTextView)
        mVideoView = view.findViewById<View>(R.id.video_preview) as VideoView
        mPhoto = view.findViewById<View>(R.id.image_photo) as ImageView
        mSwitchCamera = view.findViewById<View>(R.id.image_switch) as ImageView
        mSwitchCamera!!.setImageResource(iconSrc)
        mCaptureLayout = view.findViewById<View>(R.id.capture_layout) as CaptureLayout
        mCaptureLayout!!.setDuration(duration)
        mCaptureLayout!!.setIconSrc(iconLeft, iconRight)

//        mSwitchCamera?.run {
//
//            val lp: LinearLayout.LayoutParams = layoutParams as LinearLayout.LayoutParams
//            lp.setMargins(0, 200, 0, 0)
//            layoutParams = lp
////            mSwitchCamera?.setPadding(0, mSwitchCamera?.paddingTop ?: 0 + 150, 0, 0)
//
//        }

        //切换摄像头
        mSwitchCamera!!.setOnClickListener { switchCameraFacing() }
        //拍照 录像
        mCaptureLayout!!.setCaptureLisenter(object : CaptureListener {
            override fun takePictures() {
                mSwitchCamera!!.visibility = View.INVISIBLE
//                mFlashLamp!!.visibility = View.INVISIBLE
                //                machine.capture();
                takePicture()
            }

            override fun recordStart() {
                mSwitchCamera!!.visibility = View.INVISIBLE
//                mFlashLamp!!.visibility = View.INVISIBLE
                startRecord()
                //                machine.record(mVideoView.getHolder().getSurface(), screenProp);
            }

            override fun recordShort(time: Long) {
                mCaptureLayout?.setTextWithAnimation("录制时间过短")
                mSwitchCamera?.visibility = View.VISIBLE
//                mFlashLamp?.visibility = View.VISIBLE
                stopRecord()
                videoUrl = ""
                postDelayed({
                    //                        machine.stopRecord(true, time);
                    onResume()
                }, 1500 - time)
            }

            override fun recordEnd(time: Long) {
//                machine.stopRecord(false, time);
                stopRecord()
                mVideoView?.visibility = View.VISIBLE
                playVideo(videoUrl)

            }

            override fun recordZoom(zoom: Float) {
                Logger.i("recordZoom")
                //                machine.zoom(zoom, CameraInterface.TYPE_RECORDER);
            }

            override fun recordError() {
                if (errorLisenter != null) {
                    errorLisenter!!.AudioPermissionError()
                }
            }
        })
        //确认 取消
        mCaptureLayout!!.setTypeLisenter(object : TypeListener {
            override fun cancel() {
//                machine.cancle(mVideoView.getHolder(), screenProp);\
                if (videoUrl.isNullOrEmpty().not()) {
                    resetState(TYPE_VIDEO)
                } else {
                    resetState(TYPE_PICTURE)
                }
                onResume()
            }

            override fun confirm() {
//                machine.confirm();
                if (videoUrl.isNullOrEmpty().not()) {
                    confirmState(TYPE_VIDEO)
                } else {
                    confirmState(TYPE_PICTURE)
                }
            }
        })
        mCaptureLayout!!.setLeftClickListener {
            if (leftClickListener != null) {
                leftClickListener!!.onClick()
            }
        }
        mCaptureLayout!!.setRightClickListener {
            if (rightClickListener != null) {
                rightClickListener!!.onClick()
            }
        }

    }


    /**启动相机*/
    fun initCamera() {

        mCameraVideo = CameraVideoManager(context)
        mCameraVideo?.apply {
            setAutoFitTextureView(mAutoTextView)
            setOnCameraResultListener(object : OnCameraResultListener {
                override fun onVideoRecorded(filePath: String) {
                    /**@link recordEnd() */
                }

                override fun onPhotoTaken(bytes: ByteArray?, filePath: String?) {
                    onPause()
                    mPhoto?.visibility = View.VISIBLE
                    ImageLoader.load(mContext, filePath, mPhoto)
                    mCaptureLayout!!.startAlphaAnimation()
                    mCaptureLayout!!.startTypeBtnAnimator()
                }

            })
        }

    }

    fun startPreview() {
        mCameraVideo?.startPreview()
    }

    //切换摄像头
    fun switchCameraFacing() {
        mCameraVideo?.switchCameraFacing()
    }

    /**拍摄视频*/
    fun startRecord() {
        videoUrl = getVideoFilePath(mContext)
        mCameraVideo?.startRecordingVideo(videoUrl)
    }

    fun stopRecord() {
        mCameraVideo?.stopRecordingVideo()
    }


    /**拍照 拒绝快速重复点击*/
    fun takePicture() {
        val nowtime = System.currentTimeMillis()
        if (nowtime - mLastClickTime > TIME_INTERVAL) {
            mLastClickTime = nowtime
            picUrl = getPicFilePath(mContext)
            mCameraVideo?.takePicture(picUrl)
        }
    }


    private fun getPicFilePath(context: Context): String {
        val dir = File(context.getExternalFilesDir(null), "Photo")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir.absolutePath + "/" + System.currentTimeMillis() + ".jpeg"
    }

    private fun getVideoFilePath(context: Context): String {
        val dir = File(context.getExternalFilesDir(null), "Videos")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir.absolutePath + "/" + System.currentTimeMillis() + ".mp4"
    }


    //生命周期onResume
    fun onResume() {
        Logger.i("JCameraView onResume")
        if (isPermission) {
            if (!is2Pause) {
                mCameraVideo!!.onResume()
            }
            is2Pause = false
        }
        resetState(TYPE_DEFAULT); //重置状态
//        CameraInterface.getInstance().registerSensorManager(mContext);
//        CameraInterface.getInstance().setSwitchView(mSwitchCamera, mFlashLamp);
//        machine.start(mVideoView.getHolder(), screenProp);
    }

    //生命周期onPause
    fun onPause() {
        Logger.i("JCameraView onPause")
        if (isPermission) {
            if (!is2Pause) {
                mCameraVideo!!.onPause()
            }
        }
        resetState(TYPE_VIDEO)
        resetState(TYPE_PICTURE)
//        CameraInterface.getInstance().isPreview(false);
//        CameraInterface.getInstance().unregisterSensorManager(mContext);
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (event.pointerCount == 1) {
                    //显示对焦指示器
                    setFocusViewWidthAnimation(event.x, event.y)
                }
                if (event.pointerCount == 2) {
                    Log.i("CJT", "ACTION_DOWN = " + 2)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount == 1) {
                    firstTouch = true
                }
                if (event.pointerCount == 2) {
                    //第一个点
                    val point_1_X = event.getX(0)
                    val point_1_Y = event.getY(0)
                    //第二个点
                    val point_2_X = event.getX(1)
                    val point_2_Y = event.getY(1)
                    val result = Math.sqrt(
                        Math.pow(
                            point_1_X - point_2_X.toDouble(),
                            2.0
                        ) + Math.pow(
                            point_1_Y -
                                    point_2_Y.toDouble(), 2.0
                        )
                    ).toFloat()
                    if (firstTouch) {
                        firstTouchLength = result
                        firstTouch = false
                    }
                    if ((result - firstTouchLength).toInt() / zoomGradient != 0) {
                        firstTouch = true
                        //                        machine.zoom(result - firstTouchLength, CameraInterface.TYPE_CAPTURE);
                    }
                    //                    Log.i("CJT", "result = " + (result - firstTouchLength));
                }
            }
            MotionEvent.ACTION_UP -> firstTouch = true
        }
        return true
    }

    //对焦框指示器动画
    private fun setFocusViewWidthAnimation(x: Float, y: Float) {
//        machine.foucs(x, y, new CameraInterface.FocusCallback() {
//            @Override
//            public void focusSuccess() {
//                mFoucsView.setVisibility(INVISIBLE);
//            }
//        });
    }

    private fun updateVideoViewSize(
        videoWidth: Float,
        videoHeight: Float
    ) {
        if (videoWidth > videoHeight) {
            val videoViewParam: LayoutParams
            val height = (videoHeight / videoWidth * width).toInt()
            videoViewParam = LayoutParams(LayoutParams.MATCH_PARENT, height)
            videoViewParam.gravity = Gravity.CENTER
            mVideoView!!.layoutParams = videoViewParam
        }
    }

    /**************************************************
     * 对外提供的API                     *
     */
    fun setSaveVideoPath(path: String?) {
//        CameraInterface.getInstance().setSaveVideoPath(path);
    }

    fun setJCameraLisenter(jCameraLisenter: JCameraListener?) {
        this.jCameraLisenter = jCameraLisenter
    }

    private var errorLisenter: ErrorListener? = null

    //启动Camera错误回调
    fun setErrorLisenter(errorLisenter: ErrorListener?) {
        this.errorLisenter = errorLisenter
        //        CameraInterface.getInstance().setErrorLinsenter(errorLisenter);
    }

    //设置CaptureButton功能（拍照和录像）
    fun setFeatures(state: Int) {
        mCaptureLayout!!.setButtonFeatures(state)
    }

    //设置录制质量
    fun setMediaQuality(quality: Int) {
//        CameraInterface.getInstance().setMediaQuality(quality);
    }

    fun resetState(type: Int) {
        when (type) {
            TYPE_VIDEO -> {
                stopVideo()
                mVideoView?.visibility = View.INVISIBLE
            }
            TYPE_PICTURE -> {
                mPhoto?.visibility = View.INVISIBLE
            }
            TYPE_SHORT -> {

            }
            TYPE_DEFAULT -> {

            }
        }
        mSwitchCamera?.visibility = VISIBLE;
//        mFlashLamp?.visibility = VISIBLE;
        mCaptureLayout?.resetCaptureLayout();
    }

    fun confirmState(type: Int) {
        when (type) {
            TYPE_VIDEO -> {
                stopVideo() //停止播放
                mVideoView!!.layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
//                machine.start(mVideoView!!.holder, screenProp)
                if (jCameraLisenter != null) {
                    jCameraLisenter!!.recordSuccess(videoUrl, null)
                }
            }
            TYPE_PICTURE -> {
                mPhoto!!.visibility = View.INVISIBLE
                if (jCameraLisenter != null) {
                    jCameraLisenter!!.captureSuccess(picUrl)
                }
            }
            TYPE_SHORT -> {
            }
            TYPE_DEFAULT -> {
            }
        }
        mCaptureLayout!!.resetCaptureLayout()
    }


    fun playVideo(url: String) {
        Logger.e("video_url=$url")
//        this.firstFrame = firstFrame
        Thread(Runnable {
            try {
                if (mMediaPlayer == null) {
                    mMediaPlayer = MediaPlayer()
                } else {
                    mMediaPlayer!!.reset()
                }
                mMediaPlayer!!.setDataSource(url)
                mMediaPlayer!!.setSurface(mVideoView!!.holder.surface)
                mMediaPlayer!!.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
                mMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
                mMediaPlayer!!.setOnVideoSizeChangedListener(OnVideoSizeChangedListener { mp, width, height ->
                    updateVideoViewSize(
                        mMediaPlayer!!.videoWidth.toFloat(), mMediaPlayer!!.videoHeight.toFloat()
                    )
                })
                mMediaPlayer!!.setOnPreparedListener { mMediaPlayer!!.start() }
                mMediaPlayer!!.isLooping = true
                mMediaPlayer!!.prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }).start()
    }

    fun stopVideo() {
        if (mMediaPlayer != null && mMediaPlayer!!.isPlaying) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }


    fun setLeftClickListener(clickListener: ClickListener?) {
        leftClickListener = clickListener
    }

    fun setRightClickListener(clickListener: ClickListener?) {
        rightClickListener = clickListener
    }


}