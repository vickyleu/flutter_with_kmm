package com.example.flutter_with_kmm.connectivity

import com.example.flutter_with_kmm.domain.GrantedType
import com.example.flutter_with_kmm.domain.InternetGranted
import com.example.flutter_with_kmm.domain.SDKGateway
import com.example.flutter_with_kmm.domain.SDKNetworkGrantedType
import com.example.flutter_with_kmm.domain.SDKNetworkType
import com.example.flutter_with_kmm.domain.available
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readValue
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.CoreFoundation.CFRunLoopGetCurrent
import platform.CoreFoundation.kCFRunLoopDefaultMode
import platform.CoreTelephony.CTCellularDataRestrictedState
import platform.CoreTelephony.CellularDataRestrictionDidUpdateNotifier
import platform.CoreTelephony.CellularDataRestrictionDidUpdateNotifierVar
import platform.Foundation.NSClassFromString
import platform.Foundation.NSSelectorFromString
import platform.Foundation.valueForKey
import platform.Foundation.valueForKeyPath
import platform.SystemConfiguration.SCNetworkReachabilityFlagsVar
import platform.SystemConfiguration.SCNetworkReachabilityGetFlags
import platform.SystemConfiguration.SCNetworkReachabilityUnscheduleFromRunLoop
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsReachable
import platform.UIKit.UIDevice
import platform.UIKit.UIView
import platform.UIKit.statusBarManager
import platform.darwin.NSObject
import platform.darwin.freeifaddrs
import platform.darwin.getifaddrs
import platform.darwin.ifaddrs
import platform.darwin.inet_ntoa
import platform.posix.AF_INET
import platform.posix.sockaddr_in
import kotlin.time.Duration.Companion.seconds

internal suspend fun SDKGateway.restrictedState(delay: Boolean=false): Boolean {
    if (this.platform._cellularData.cellularDataRestrictionDidUpdateNotifier == null) {
        this.platform._cellularData.cellularDataRestrictionDidUpdateNotifier = {
            this.platform.logger.e { "state:$it" }
            scope.launch {
                withContext(Dispatchers.IO) {
                    when (it) {
                        CTCellularDataRestrictedState.kCTCellularDataNotRestricted -> {
                            this@restrictedState.platform.logger.e { "网络权限已开启" }
                            this@restrictedState.interactor.callBridge(
                                InternetGranted,
                                mapOf(GrantedType to SDKNetworkGrantedType.Accessible)
                            )
                        }

                        CTCellularDataRestrictedState.kCTCellularDataRestrictedStateUnknown -> {
                            this@restrictedState.platform.logger.e { "网络权限未知" }
                            this@restrictedState.interactor.callBridge(
                                InternetGranted,
                                mapOf(GrantedType to SDKNetworkGrantedType.Unknown)
                            )
                        }

                        CTCellularDataRestrictedState.kCTCellularDataRestricted -> {
                            this@restrictedState.platform.logger.e { "网络权限已关闭" }
                            this@restrictedState.interactor.callBridge(
                                InternetGranted,
                                mapOf(GrantedType to SDKNetworkGrantedType.Restricted)
                            )
                        }

                        else -> Unit
                    }
                }
            }
        }
        this.platform.logger.e { "网络权限监听已开启 ${this.platform._cellularData.cellularDataRestrictionDidUpdateNotifier}" }
//        CellularDataRestrictionDidUpdateNotifierVar
        /*this.platform._cellularData.setCellularDataRestrictionDidUpdateNotifier {

        }*/
    }
    if (this.platform.isFirstRun) {
        this.platform.logger.error { "网络检测" }
        return withContext(Dispatchers.IO) {
            this@restrictedState.platform.waitActive()
            this@restrictedState.platform.isFirstRun = false
            restrictedState(delay=true)
        }
    } else {
        return when (this@restrictedState.platform._cellularData.restrictedState()) {
            CTCellularDataRestrictedState.kCTCellularDataRestricted -> {// 无蜂窝数据,访问受限制
                this@restrictedState.platform.logger.error { ":::::>>>>>>:::::无蜂窝数据访问权限" }
                val type = getCurrentNetworkType()
                /*  若用户是通过蜂窝数据 或 WLAN 上网，走到这里来 说明权限被关闭 SDKNetworkGrantedType.Restricted **/
                // 可能开了飞行模式，无法判断 SDKNetworkGrantedType.Unknown
                type == SDKNetworkType.Cellular || type == SDKNetworkType.WIFI
            }

            CTCellularDataRestrictedState.kCTCellularDataNotRestricted -> {// 蜂窝数据访问不受限制，那就必定有 Wi-Fi 数据访问权限
                this@restrictedState.platform.logger.error { ":::::>>>>>>:::::有蜂窝数据访问权限" }
                true
            }

            CTCellularDataRestrictedState.kCTCellularDataRestrictedStateUnknown -> {// CTCellularData 刚开始初始化的时候，可能会拿到 kCTCellularDataRestrictedStateUnknown 延迟一下再试就好了
                if(delay){
                    false
                }else{
                    this@restrictedState.platform.waitActive()
                    restrictedState(delay=true)
                }
            }

            else -> false
        }
    }
}

suspend fun SDKGateway.getCurrentNetworkType(delay: Boolean = false): SDKNetworkType {
    return if (isWiFiEnable()) {
        SDKNetworkType.WIFI
    } else {
        val type = getNetworkTypeFromStatusBar()
        if (type == SDKNetworkType.WIFI) { // // wifi关闭的情况下,这时候从状态栏拿到的是 Wi-Fi 说明状态栏没有刷新，延迟一会再获取
            if (delay) type
            else
                withContext(Dispatchers.IO) {
                    delay(0.1.seconds)
                    return@withContext withContext(Dispatchers.Main) inside@{
                        return@inside getCurrentNetworkType(delay = true)
                    }
                }
        } else {
            type
        }
    }
}

/**
判断用户是否连接到 Wi-Fi
 */
suspend fun SDKGateway.isWiFiEnable(): Boolean {
    return (wiFiIPAddress()?.length ?: 0) > 0
}

suspend fun SDKGateway.getNetworkTypeFromStatusBar(): SDKNetworkType {
    var type = 0
    try {
        val statusBar = if (available(13.0f)) {
            withContext(Dispatchers.Main){
                val statusBarManager = this@getNetworkTypeFromStatusBar.platform.app.keyWindow?.windowScene?.statusBarManager
                val selector = NSSelectorFromString("createLocalStatusBar")
                val statusBarSelector = NSSelectorFromString("statusBar")
                if (statusBarManager != null && statusBarManager.respondsToSelector(selector)) {
                    val _localStatusBar = statusBarManager.performSelector(selector) as? NSObject
                    if (_localStatusBar != null && _localStatusBar.respondsToSelector(statusBarSelector)) {
                        _localStatusBar.performSelector(statusBarSelector)
                    } else {
                        null
                    }
                } else null
            }
        } else {
            withContext(Dispatchers.Main){
                this@getNetworkTypeFromStatusBar.platform.app.valueForKey("statusBar")
            }
        } as? UIView ?: return SDKNetworkType.Unknown

        val isModernStatusBar = statusBar.isKindOfClass(NSClassFromString("UIStatusBar_Modern"))
        if (isModernStatusBar) {
            val currentData: NSObject =
                statusBar.valueForKeyPath("statusBar.currentData") as NSObject
            val wifiEnable = currentData.valueForKeyPath("_wifiEntry.isEnabled") as? Boolean
            val cellularEnable = currentData.valueForKeyPath("_cellularEntry.type") as? Boolean
            return if (wifiEnable == true) {
                SDKNetworkType.WIFI
            } else if (cellularEnable == true) {
                SDKNetworkType.Cellular
            } else {
                SDKNetworkType.Offline
            }
        } else {
            return  withContext(Dispatchers.Main){
                val children = (statusBar.valueForKeyPath("foregroundView") as? UIView)?.subviews
                children?.filterNotNull()?.map {
                    it as NSObject
                }?.forEach {
                    if (it.isKindOfClass(NSClassFromString("UIStatusBarDataNetworkItemView"))) {
                        type = it.valueForKeyPath("dataNetworkType") as? Int ?: 0
                    }
                }
                return@withContext when (type) {
                    0 -> SDKNetworkType.Offline
                    5 -> SDKNetworkType.WIFI
                    else -> SDKNetworkType.Cellular
                }
            }
        }
    } catch (e: Exception) {
        return SDKNetworkType.Unknown
    }
}

suspend fun SDKGateway.wiFiIPAddress(): String? {
    try {
        val interfaces = nativeHeap.alloc<CPointerVar<ifaddrs>>()
        val status = getifaddrs(interfaces.ptr)
        var ipAddress: String? = null
        if (status == 0) {
            var temp: ifaddrs? = interfaces.pointed
            while (temp != null) {
                if ((temp.ifa_addr?.pointed?.sa_family?.toInt()) == AF_INET) {
                    if (temp.ifa_name?.pointed?.value.toString() == "en0") {
                        val ptr = temp.ifa_addr!!.pointed
                        val sin = ptr.reinterpret<sockaddr_in>()
                        val cValue = sin.sin_addr.readValue()
                        ipAddress = inet_ntoa(cValue)?.toKString()
                    }
                }
                temp = temp.ifa_next?.pointed
            }
        }
        // TODO 释放内存, 但是会报错
//        freeifaddrs(interfaces.ptr.pointed.pointed?.readValue())

        if (ipAddress.isNullOrEmpty()) {
            return null
        }
        return ipAddress
    } catch (e: Exception) {
        return null
    }
}

suspend fun SDKGateway.currentReachable(): Boolean {
    if (UIDevice.currentDevice.systemVersion.toFloat() < 10.0 || this.platform.isSimulator()) {
        return true
    }
    val flags = nativeHeap.alloc<SCNetworkReachabilityFlagsVar>()
    if (SCNetworkReachabilityGetFlags(this.platform._reachabilityRef, flags.ptr)) {
        val f = flags.value.toInt()
        nativeHeap.free(flags.rawPtr)
        return (f and kSCNetworkReachabilityFlagsReachable.toInt()) != 0
    }
    return false
}

suspend fun SDKGateway.stopReachable() {
    if (this.platform._reachabilityRef != null) {
        SCNetworkReachabilityUnscheduleFromRunLoop(
            this.platform._reachabilityRef,
            CFRunLoopGetCurrent(),
            kCFRunLoopDefaultMode
        )
    }
}