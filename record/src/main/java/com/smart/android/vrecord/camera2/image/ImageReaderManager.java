package com.smart.android.vrecord.camera2.image;

import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import android.os.FileUtils;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;

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


    private ImageReader mImageReader;

    private void setupImageReader(Size mPreviewSize){

        //前三个参数分别是需要的尺寸和格式，最后一个参数代表每次最多获取几帧数据
        mImageReader=ImageReader.newInstance(mPreviewSize.getWidth(),mPreviewSize.getHeight(), ImageFormat.JPEG,1);

        //监听ImageReader的事件，当有图像流数据可用时会回调onImageAvailable方法，它的参数就是预览帧数据，可以对这帧数据进行处理
        mImageReader.setOnImageAvailableListener(reader -> {
            Image image=reader.acquireLatestImage();


        },null);

    }


}
