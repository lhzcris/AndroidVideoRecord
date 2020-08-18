package com.smart.android.vrecord.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * @author liuhuazhong
 * @since 20200818
 */
public class SystemUtil {


    ///////////////////////////////////////////////////////////////////////////
    // EMUI
    ///////////////////////////////////////////////////////////////////////////
    private static final String KEY_EMUI_API_LEVEL = "ro.build.hw_emui_api_level";
    private static final String KEY_EMUI_VERSION = "ro.build.version.emui";
    private static final String KEY_EMUI_CONFIG_HW_SYS_VERSION = "ro.confg.hw_systemversion";

    /**
     * 个推sdk中判断华为emui方法 copy
     * @see RomUtils
     * @return
     */
    public static boolean isHuaWei() {
        return (!TextUtils.isEmpty(getSystemProperty(KEY_EMUI_API_LEVEL))
                || !TextUtils.isEmpty(getSystemProperty(KEY_EMUI_VERSION))
                || !TextUtils.isEmpty(getSystemProperty(KEY_EMUI_CONFIG_HW_SYS_VERSION)));
    }

    private static String getSystemProperty(final String name) {
        String prop = getSystemPropertyByShell(name);
        if (!TextUtils.isEmpty(prop)) {
            return prop;
        }
        prop = getSystemPropertyByStream(name);
        if (!TextUtils.isEmpty(prop)) {
            return prop;
        }
        if (Build.VERSION.SDK_INT < 28) {
            return getSystemPropertyByReflect(name);
        }
        return prop;
    }

    private static String getSystemPropertyByShell(final String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
        } catch (IOException e) {
            return "";
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ignore) {

                }
            }
        }

        return line;
    }

    private static String getSystemPropertyByStream(final String key) {
        try {
            Properties prop = new Properties();
            FileInputStream is = new FileInputStream(
                    new File(Environment.getRootDirectory(), "build.prop")
            );
            prop.load(is);
            return prop.getProperty(key, "");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String getSystemPropertyByReflect(String key) {
        try {
            @SuppressLint("PrivateApi")
            Class<?> clz = Class.forName("android.os.SystemProperties");
            Method get = clz.getMethod("get", String.class, String.class);
            return (String) get.invoke(clz, key, "");
        } catch (Exception e) {
            return "";
        }
    }


}
