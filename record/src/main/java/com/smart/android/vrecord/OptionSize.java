package com.smart.android.vrecord;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author liuhuazhong
 * @since 20200616
 * <p>
 * 输出尺寸
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({OptionSize.size4_3, OptionSize.size16_9, OptionSize.sizeFull})
public @interface OptionSize {
    int size4_3 = 100;
    int size16_9 = 101;
    int size1_1 = 102;
    int sizeFull = 103;
}
