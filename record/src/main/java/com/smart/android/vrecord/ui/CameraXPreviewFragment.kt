//package com.smart.android.vrecord.ui
//
//import android.graphics.Matrix
//import android.os.Bundle
//import android.util.Rational
//import android.util.Size
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.camera.core.AspectRatio
//import androidx.camera.core.CameraX.bindToLifecycle
//import androidx.camera.core.Preview
//import androidx.camera.core.impl.PreviewConfig
//import androidx.fragment.app.Fragment
//import com.smart.android.vrecord.R
//import kotlinx.android.synthetic.main.cx_fragment_preview.*
//
//
///**
// * @author liuhuazhong
// * @since 20200610
// */
//class CameraXPreviewFragment : Fragment() {
//
//    companion object {
//        fun newInstance() = CameraXPreviewFragment()
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.cx_fragment_preview, container, false);
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        initUI(view)
//
//    }
//
//    fun initUI(view: View) {
//        view_finder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
//            updateTransform()
//        }
//        view_finder.post { startCamera() }
//    }
//
//
//    private fun updateTransform() {
//        val matrix = Matrix()
//        // Compute the center of the view finder
//        val centerX: Float = view_finder.width / 2f
//        val centerY: Float = view_finder.height / 2f
//        val rotations = floatArrayOf(0f, 90f, 180f, 270f)
//        // Correct preview output to account for display rotation
//        val rotationDegrees = rotations[view_finder.display.rotation]
//        matrix.postRotate(-rotationDegrees, centerX, centerY)
//        // Finally, apply transformations to our TextureView
//        view_finder.setTransform(matrix)
//    }
//
//
//    private fun startCamera() { // 1. preview
//        val previewConfig = Preview.Builder()
////            .setTargetAspectRatio(Rational(1, 1))
//            .setTargetResolution(Size(640, 640))
//            .build()
////        val previewConfig = PreviewConfig.Builder().apply {
////            setTargetAspectRatio(AspectRatio.RATIO_4_3)
////        }.build()
//
////        val preview = Preview(previewConfig)
////        preview.onPreviewOutputUpdateListener =
////            OnPreviewOutputUpdateListener { output ->
////                val parent = view_finder.parent as ViewGroup
////                parent.removeView(view_finder)
////                parent.addView(view_finder, 0)
////                view_finder.surfaceTexture = output.surfaceTexture
////                updateTransform()
////            }
////        bindToLifecycle(this, preview)
//    }
//
//}