package com.example.flutter_with_kmm.domain

import com.example.flutter_with_kmm.connectivity.currentReachable
import com.example.flutter_with_kmm.connectivity.restrictedState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.UIKit.UIDevice

fun available(version: Float): Boolean {
    return UIDevice.currentDevice.systemVersion.toFloat().compareTo(other = version) == 1
}


actual suspend fun SDKGateway.isInternetGranted(): Boolean {
    if (UIDevice.currentDevice.systemVersion.toFloat() < 10.0 || this.currentReachable()) {
        return true
    }
    return this.scope.launch {
        withContext(Dispatchers.IO) {
            this@isInternetGranted.restrictedState()
        }
    }.start()
}

