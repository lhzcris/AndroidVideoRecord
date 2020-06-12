package com.smart.android.vrecord.ui

/**
 * @author liuhuazhong
 * @since 20200611
 */
interface PreViewOnChanagerListener {

    fun onPreviewResume()

    fun onPreviewPause()

//    fun startRecord();

//    fun stopRecord();

    fun progressChangeListener(duration: Int);

//    fun reStartPreview();

}