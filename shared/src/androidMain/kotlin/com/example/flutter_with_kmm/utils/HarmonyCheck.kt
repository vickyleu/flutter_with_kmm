package com.example.flutter_with_kmm.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import java.lang.reflect.Method


/**
 * 鸿蒙系统检查
 */
object HarmonyCheck {
    /**
     * 是否为鸿蒙系统
     *
     * @return true为鸿蒙系统
     */
    fun isHarmonyOs(): Boolean {
        return try {
            val buildExClass = Class.forName("com.huawei.system.BuildEx")
            val osBrand = buildExClass.getMethod("getOsBrand").invoke(buildExClass)
            "Harmony".equals(osBrand?.toString(), ignoreCase = true)
        } catch (x: ClassNotFoundException) {
            false
        } catch (x: Throwable) {
            x.printStackTrace()
            false
        }
    }

    /**
     * 获取鸿蒙系统版本号
     *
     * @return 版本号
     */
    fun getHarmonyVersion(): String {
        return getProp("hw_sc.build.platform.version", "")
    }

    /**
     * 获取鸿蒙系统版本号（含小版本号，实际上同Android的android.os.Build.DISPLAY）
     *
     * @return 版本号
     */
    fun getHarmonyAccurateVersion(): String {
        return getHarmonyDisplayVersion().let {
            if (it.isNotEmpty()) {
                val subString = it.substring(it.indexOf(getHarmonyVersion()), it.length)
                val harmonyVersion = subString.substring(0, subString.indexOf("("))
                harmonyVersion
            }else{
                 "0.0.0"
            }
        }
    }
    /**
     * 判断是否开启鸿蒙纯净模式
     */
    fun isPureMode(context: Context?): Boolean {
        var result = false
        if (!isHarmonyOs()) {
            return false
        }
        try {
            if (context != null) {
                result = 0 == Settings.Secure.getInt(context.contentResolver, "pure_mode_state", 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
    /**
     * 获得鸿蒙系统版本号（含小版本号，实际上同Android的android.os.Build.DISPLAY）
     * @return 版本号
     */
    fun getHarmonyDisplayVersion(): String {
        return Build.DISPLAY
    }

    /**
     * 获取属性
     * @param property
     * @param defaultValue
     * @return
     */
    @Suppress("SameParameterValue")
    @SuppressLint("PrivateApi")
    private fun getProp(property: String, defaultValue: String): String {
        try {
            val spClz = Class.forName("android.os.SystemProperties")
            val method: Method = spClz.getDeclaredMethod("get", String::class.java)
            val value = method.invoke(spClz, property) as String
            return if (TextUtils.isEmpty(value)) {
                defaultValue
            } else value
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return defaultValue
    }

}