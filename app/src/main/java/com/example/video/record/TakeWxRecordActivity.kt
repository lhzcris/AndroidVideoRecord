package com.example.video.record

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.smart.android.utils.ToastUtils
import com.smart.android.vrecord.listener.CaptureListener
import com.smart.android.vrecord.ui.CBaseActivity
import kotlinx.android.synthetic.main.activity_take_wx_record.*

class TakeWxRecordActivity : CBaseActivity() {


    override fun layout() = R.layout.activity_take_wx_record

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        jcameraview.apply {
            setLeftClickListener {
                finish()
            }
            setRightClickListener {
                ToastUtils.showShort("right")
            }
        }
    }


    override fun onResume() {
        super.onResume()
        jcameraview.onResume()
    }

    override fun onPause() {
        super.onPause()
        jcameraview.onPause()
    }


}