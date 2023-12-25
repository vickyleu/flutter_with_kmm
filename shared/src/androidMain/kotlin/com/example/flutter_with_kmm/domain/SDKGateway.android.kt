package com.example.flutter_with_kmm.domain

import com.example.flutter_with_kmm.utils.isNetworkEnable
import com.example.flutter_with_kmm.utils.registerNetworkReceiver
import com.example.flutter_with_kmm.utils.unregisterNetworkReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


actual suspend fun SDKGateway.isInternetGranted(): Boolean {
   return withContext(Dispatchers.IO){
        val isNetworkEnable = this@isInternetGranted.platform.app.isNetworkEnable()
       // 添加广播监听网络权限关闭打开的事件
       this@isInternetGranted.platform.app.registerNetworkReceiver(this) {
           this@isInternetGranted.scope.value.launch {
               withContext(Dispatchers.IO) {
                   this@isInternetGranted.interactor.callBridge(
                       InternetGranted,
                       mapOf(GrantedType to (if (it) SDKNetworkGrantedType.Accessible else SDKNetworkGrantedType.Restricted).name)
                   )
               }
           }
       }.apply {
           this@isInternetGranted.platform.networkReceiver = this
       }
       return@withContext isNetworkEnable
    }
}

internal actual fun SDKGateway.platformDestroy() {
    this.platform.app.unregisterNetworkReceiver(this.platform.networkReceiver)
}