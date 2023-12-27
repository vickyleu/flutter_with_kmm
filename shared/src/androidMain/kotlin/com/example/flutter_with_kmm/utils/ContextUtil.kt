package com.example.flutter_with_kmm.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.lang.reflect.Field
import java.net.URL

object SystemServiceLister {
    fun listAllServices(context: Context) {
        val fields: Array<Field> = Context::class.java.declaredFields
        val list = mutableListOf<String>()
        for (field in fields) {
            try {
                field.isAccessible = true
                val value: Any? = field.get(context)
                if (value is String && field.name.endsWith("_SERVICE")) {
                    val serviceName = field.name.split("Context.").last()
                    list.add("Context.$serviceName")
                }
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
        list.joinToString("\n").let {
            Log.e("SystemServiceLister", it)
        }
    }
}

/**
 * 检查网络权限是否打开,不检查网络是否可用,需要处理miui 13的网络权限问题,每一句代码都加上注释
 */
@SuppressLint("ObsoleteSdkInt")
suspend fun Context.isNetworkEnable(): Boolean {
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
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED).let {
            if (it) {
                // 检查红米手机联网权限是否打开,仅wifi,仅移动网络,wifi和移动网络,都没有,这四种状态的检查,不是流量统计权限
                if (Build.BRAND.equals("xiaomi", ignoreCase = true) || Build.BRAND.equals(
                        "redmi",
                        ignoreCase = true
                    )
                ) {
                    return@let withContext(Dispatchers.IO) {
                        // ping一下百度,如果能ping通,说明网络权限打开
                        val runtime = Runtime.getRuntime();
                        try {
                            val ipProcess = runtime.exec("ping -c 3 www.baidu.com")
                            val exitValue = ipProcess.waitFor()
                            Log.i("Available", "Process:$exitValue")
                            return@withContext (exitValue == 0)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        return@withContext false
                    }
                } else {
                    return@let true
                }
            } else {
                return@let false
            }
        }
    } else {
        // 获取当前网络状态
        val networkInfo = connectivityManager?.activeNetworkInfo ?: return false
        // 判断当前网络是否已经连接
        return networkInfo.isAvailable.let {
            if (it) {
                // 检查红米手机联网权限是否打开,仅wifi,仅移动网络,wifi和移动网络,都没有,这四种状态的检查,不是流量统计权限
                if (Build.BRAND.equals("xiaomi", ignoreCase = true) || Build.BRAND.equals(
                        "redmi",
                        ignoreCase = true
                    )
                ) {
                    return@let withContext(Dispatchers.IO) {
                        // ping一下百度,如果能ping通,说明网络权限打开
                        val runtime = Runtime.getRuntime();
                        try {
                            val ipProcess = runtime.exec("ping -c 3 www.baidu.com")
                            val exitValue = ipProcess.waitFor()
                            Log.i("Available", "Process:$exitValue")
                            return@withContext (exitValue == 0)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        return@withContext false
                    }
                } else {
                    return@let true
                }
            } else {
                return@let false
            }
        }
    }
}


/**
 * 注册网络权限打开关闭的广播
 */
@SuppressLint("ObsoleteSdkInt")
fun Context.registerNetworkReceiver(
    coroutineScope: CoroutineScope,
    listener: (Boolean) -> Unit = {}
): Any {
    // 通过Context监听页面前台后台切换
    (this.applicationContext as Application).apply {
        this.registerActivityLifecycleCallbacks(object : SimpleActivityLifecycleCallbacks() {
            override fun onActivityResumed(activity: android.app.Activity) {
                println("onActivityResumed ::${activity.localClassName}")
                // 网络权限打开关闭的广播
                coroutineScope.launch {
                    withContext(Dispatchers.IO) {
                        val isEnable = isNetworkEnable()
                        println("onActivityResumed ::${activity.localClassName}  isEnable:$isEnable")
                        listener.invoke(isEnable)
                    }
                }
            }
        })
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val connectivityManager = getService<ConnectivityManager>(Context.CONNECTIVITY_SERVICE)
        val networkReceiver = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                // 网络权限打开
                val cm = connectivityManager ?: return
                val cap = cm.getNetworkCapabilities(network) ?: return
                coroutineScope.launch {
                    withContext(Dispatchers.IO){
                        when {
                            cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> { // wifi网络
                                try {
                                    network.openConnection(URL("https://www.baidu.com")).connect()
                                    listener.invoke(true)
                                }catch (e:Exception){
                                    e.printStackTrace()
                                    listener.invoke(false)
                                }
                            }

                            cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> { //数据网络
                                try {
                                    network.openConnection(URL("https://www.baidu.com")).connect()
                                    listener.invoke(true)
                                }catch (e:Exception){
                                    e.printStackTrace()
                                    listener.invoke(false)
                                }
                            }

                            cap.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> { //以太网
                                try {
                                    network.openConnection(URL("https://www.baidu.com")).connect()
                                    listener.invoke(true)
                                }catch (e:Exception){
                                    e.printStackTrace()
                                    listener.invoke(false)
                                }
                            }
                        }
                    }
                }
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
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            listener.invoke(isNetworkEnable())
                        }
                    }

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
        connectivityManager?.unregisterNetworkCallback(
            (listener as? ConnectivityManager.NetworkCallback) ?: return
        )
    } else {
        unregisterReceiver(listener as? BroadcastReceiver)
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> Context.getService(name: String): T? {
    return getSystemService(name) as? T
}