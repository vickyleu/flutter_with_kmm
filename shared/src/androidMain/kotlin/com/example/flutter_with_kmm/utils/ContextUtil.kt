package com.example.flutter_with_kmm.utils

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build


/**
 * 检查网络权限是否打开,不检查网络是否可用,需要处理miui 13的网络权限问题,每一句代码都加上注释
 */
@SuppressLint("ObsoleteSdkInt")
fun Context.isNetworkEnable(): Boolean {
    // 获取系统的网络服务
    val connectivityManager = getService<ConnectivityManager>(Context.CONNECTIVITY_SERVICE)
    // 兼容性处理
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // 获取当前网络状态
        val network = connectivityManager?.activeNetwork ?: return false
        // 获取当前网络连接的信息
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        // 判断当前网络是否已经连接
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    } else {
        // 获取当前网络状态
        val networkInfo = connectivityManager?.activeNetworkInfo ?: return false
        // 判断当前网络是否已经连接
        return networkInfo.isConnected
    }
}


/**
 * 注册网络权限打开关闭的广播
 */
@SuppressLint("ObsoleteSdkInt")
fun Context.registerNetworkReceiver(listener: (Boolean) -> Unit = {}): Any {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val connectivityManager = getService<ConnectivityManager>(Context.CONNECTIVITY_SERVICE)
        val networkReceiver = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                // 网络权限打开
                listener.invoke(true)
            }

            override fun onUnavailable() {
                // 网络权限关闭
                listener.invoke(false)
            }
        }
        connectivityManager?.registerDefaultNetworkCallback(networkReceiver)
        return networkReceiver
    } else {
        // 注册广播
        val networkReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // 网络权限打开关闭的广播
                if (intent?.action == ConnectivityManager.CONNECTIVITY_ACTION) {
                    listener.invoke(isNetworkEnable())
                }
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, intentFilter)
        return networkReceiver
    }
}
/**
 * 注册网络权限打开关闭的广播
 */
@SuppressLint("ObsoleteSdkInt")
fun Context.unregisterNetworkReceiver(listener: Any?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val connectivityManager = getService<ConnectivityManager>(Context.CONNECTIVITY_SERVICE)
        connectivityManager?.unregisterNetworkCallback((listener as? ConnectivityManager.NetworkCallback)?:return)
    } else {
        unregisterReceiver(listener as? BroadcastReceiver)
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> Context.getService(name: String): T? {
    return getSystemService(name) as? T
}