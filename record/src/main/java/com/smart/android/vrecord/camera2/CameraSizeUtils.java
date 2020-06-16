package com.smart.android.vrecord.camera2;

import android.util.Log;
import android.util.Size;

import com.smart.android.utils.Logger;
import com.smart.android.utils.Utility;
import com.smart.android.vrecord.OptionSize;
import com.smart.android.vrecord.VideoRecordPicker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Hyu on 2019-08-17.
 * Email: fvaryu@163.com
 */
public final class CameraSizeUtils {
//    private static final String TAG = CameraSizeUtils.class.getName();

    /**
     * 默认取16：9的尺寸，没有的取降序最后一个寸尺
     *
     * @param choices
     * @return
     */
    @Deprecated
    public static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            Logger.e("video size width=" + size.getWidth() + ",height=" + size.getHeight());
            if (size.getWidth() == size.getHeight() * 16 / 9 && size.getWidth() <= 1920) {
                return size;
            }
        }
        return choices[choices.length - 1];
    }


    public static Size chooseMeadiaSzie(Size[] choices, int screenHeight, int screenWidth) {
        Logger.e("screenWidth=" + screenWidth + ",screenHeight=" + screenHeight);
        int oSize = VideoRecordPicker.getInstance().getOptionSize();
        if (oSize == OptionSize.size4_3) {//4:3
            for (Size size : choices) {
                if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1280) { //1280/960
                    return size;
                }
            }
        } else if (oSize == OptionSize.sizeFull) {//全屏

            return getNearFullSize(choices, screenHeight, screenWidth);

        } else if (oSize == OptionSize.size1_1) {//1:1
            for (Size size : choices) {
                if (size.getWidth() == size.getHeight()) {
                    return size;
                }
            }
        } else {//16:9
            for (Size size : choices) {
                if (size.getWidth() == size.getHeight() * 16 / 9 && size.getWidth() <= 1920) {//1920/1080
                    return size;
                }
            }
        }

        return choices[choices.length - 1];
    }

    private static Size getNearFullSize(Size[] choices, int screenHeight, int screenWidth) {
        Size nearSize = choices[choices.length - 1];
        double tmp = 0;
        double mindiff = 100;
        double x_d_y = (double) screenWidth / (double) screenHeight;
//        Log.e("div", screenDiv + ",," + x_d_y);
        for (int i = choices.length - 1; i >= 0; i--) {//倒叙 取最接近的比例长得分辨率最高的那个
            Size size = choices[i];
            tmp = Utility.roundTwo(Math.abs((double) size.getHeight() / (double) size.getWidth() - x_d_y));
            if (tmp < mindiff) {
                mindiff = tmp;
                nearSize = size;
            }
        }
        return nearSize;
    }

    /**
     * 预览尺寸
     * <p>
     * 取配置尺寸中最大的一个尺寸，没有的话取降序第一个尺寸
     *
     * @param choices
     * @param aspectRatio
     * @return
     */
    public static Size chooseOptimalSize(Size[] choices, Size aspectRatio) {
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            Logger.d("preview size width=" + option.getWidth() + ",height=" + option.getHeight());
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
            Logger.d("未找到合适的预览尺寸");
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
