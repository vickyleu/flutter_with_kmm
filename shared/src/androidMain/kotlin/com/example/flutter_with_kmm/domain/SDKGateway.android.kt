package com.example.flutter_with_kmm.domain

import com.example.flutter_with_kmm.utils.isNetworkEnable
import com.example.flutter_with_kmm.utils.registerNetworkReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

actual suspend fun SDKGateway.isInternetGranted(): Boolean {
    val isNetworkEnable = this.platform.app.isNetworkEnable()
    if (isNetworkEnable) {
        return true
    } else {
        // 添加广播监听网络权限关闭打开的事件
        this.platform.app.registerNetworkReceiver {
            this.scope.launch {
                withContext(Dispatchers.IO) {
                    this@isInternetGranted.interactor.callBridge(
                        InternetGranted,
                        mapOf(GrantedType to (if (it) SDKNetworkGrantedType.Accessible else SDKNetworkGrantedType.Restricted).name)
                    )
                }
            }
        }
        return false
    }
}