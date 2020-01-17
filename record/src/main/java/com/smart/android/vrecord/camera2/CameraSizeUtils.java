package com.smart.android.vrecord.camera2;

import android.util.Log;
import android.util.Size;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Hyu on 2019-08-17.
 * Email: fvaryu@163.com
 */
public final class CameraSizeUtils {
    private static final String TAG = CameraSizeUtils.class.getName();

    /**
     * 默认取16：9的尺寸，没有的取降序最后一个寸尺
     *
     * @param choices
     * @return
     */
   public static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            Log.d(TAG, "video size width=" + size.getWidth() + ",height=" + size.getHeight());
//            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
//            if (size.getWidth() == size.getHeight() * 16 / 9 && size.getWidth() <= 1080) {
            if (size.getWidth() == size.getHeight() * 16 / 9 && size.getWidth() <= 1920) {
                return size;
            }
        }
        return choices[choices.length - 1];
    }

    /**
     * 取16：9尺寸中最大的一个尺寸，没有的话取降序第一个尺寸
     * @param choices
     * @param aspectRatio
     * @return
     */
   public static Size chooseOptimalSize(Size[] choices,Size aspectRatio) {
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            Log.d(TAG, "preview size width=" + option.getWidth() + ",height=" + option.getHeight());
//            if (option.getHeight() == option.getWidth() * h / w &&
//                    option.getWidth() >= width && option.getHeight() >= height) {
//                bigEnough.add(option);
//            }
            if (option.getHeight() == option.getWidth() * h / w) {
                bigEnough.add(option);
            }
        }

        if (bigEnough.size() > 0) {
            //区输出尺寸比例一致中最小的尺寸
            return Collections.max(bigEnough, new CompareSizesByArea());
        } else {
            return choices[0];
        }
    }


    static Size setOptimalPreviewSize(Size[] sizes, int previewViewWidth, int previewViewHeight) {
        List<Size> bigEnoughSizes = new ArrayList<>();
        List<Size> notBigEnoughSizes = new ArrayList<>();

        for (Size size : sizes) {
            if (size.getWidth() >= previewViewWidth && size.getHeight() >= previewViewHeight) {
                bigEnoughSizes.add(size);
            } else {
                notBigEnoughSizes.add(size);
            }
        }

        if (bigEnoughSizes.size() > 0) {
            return Collections.min(bigEnoughSizes, new CompareSizesByArea());
        } else if (notBigEnoughSizes.size() > 0) {
            return Collections.max(notBigEnoughSizes, new CompareSizesByArea());
        } else {
            Log.d(TAG, "未找到合适的预览尺寸");
            return sizes[0];
        }
    }

    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }


    }

}
