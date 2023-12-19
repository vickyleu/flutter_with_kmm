package com.example.flutter_with_kmm.domain

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
import kotlinx.coroutines.withContext
import platform.CoreTelephony.CTCellularDataRestrictedState
import platform.Foundation.NSClassFromString
import platform.Foundation.NSDate
import platform.Foundation.NSSelectorFromString
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.valueForKey
import platform.Foundation.valueForKeyPath
import platform.SystemConfiguration.SCNetworkReachabilityFlagsVar
import platform.SystemConfiguration.SCNetworkReachabilityGetFlags
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

actual suspend fun SDKGateway.isInternetGranted(): Boolean {

    if (UIDevice.currentDevice.systemVersion.toFloat() < 10.0 || currentReachable()) {
        return true
    }
    val state: CTCellularDataRestrictedState = this.platform._cellularData.restrictedState
    return when (state) {
        CTCellularDataRestrictedState.kCTCellularDataNotRestricted -> {// 系统 API 返回 无蜂窝数据访问权限
            platform.logger.error {":::::>>>>>>:::::无蜂窝数据访问权限"}
            val type = getCurrentNetworkType()
            /*  若用户是通过蜂窝数据 或 WLAN 上网，走到这里来 说明权限被关闭**/
            if (type == SDKNetworkType.Cellular || type == SDKNetworkType.WIFI) {
//                SDKNetworkGrantedType.Restricted
                false
            } else {  // 可能开了飞行模式，无法判断
//                SDKNetworkGrantedType.Unknown
                false
            }
        }

        CTCellularDataRestrictedState.kCTCellularDataRestricted -> {// 系统 API 访问有有蜂窝数据访问权限，那就必定有 Wi-Fi 数据访问权限
            platform.logger.error {":::::>>>>>>:::::有蜂窝数据访问权限"}
            true
        }
        CTCellularDataRestrictedState.kCTCellularDataRestrictedStateUnknown -> {// CTCellularData 刚开始初始化的时候，可能会拿到 kCTCellularDataRestrictedStateUnknown 延迟一下再试就好了
            this.platform.waitActive()
            isInternetGranted()
        }
        else -> false
    }
}

suspend fun SDKGateway.getCurrentNetworkType(): SDKNetworkType {
    return if (isWiFiEnable()) {
        SDKNetworkType.WIFI
    } else {
        val type = getNetworkTypeFromStatusBar()
        if (type == SDKNetworkType.WIFI) { // // wifi关闭的情况下,这时候从状态栏拿到的是 Wi-Fi 说明状态栏没有刷新，延迟一会再获取
            withContext(Dispatchers.IO) {
                delay(0.1.seconds)
                withContext(Dispatchers.Main) {
                    getCurrentNetworkType()
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

fun available(version: Float): Boolean {
    return UIDevice.currentDevice.systemVersion.toFloat().compareTo(other = version) == -1
}

suspend fun SDKGateway.getNetworkTypeFromStatusBar(): SDKNetworkType {
    var type = 0
    try {
        val statusBar = if (available(13.0f)) {
            val statusBarManager = this.platform.app.keyWindow?.windowScene?.statusBarManager
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
        } else {
            this.platform.app.valueForKey("statusBar")
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
            val children = (statusBar.valueForKeyPath("foregroundView") as? UIView)?.subviews
            children?.filterNotNull()?.map {
                it as NSObject
            }?.forEach {
                if (it.isKindOfClass(NSClassFromString("UIStatusBarDataNetworkItemView"))) {
                    type = it.valueForKeyPath("dataNetworkType") as? Int ?: 0
                }
            }
            return when (type) {
                0 -> SDKNetworkType.Offline
                5 -> SDKNetworkType.WIFI
                else -> SDKNetworkType.Cellular
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
        freeifaddrs(interfaces.ptr.pointed.pointed?.readValue())
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