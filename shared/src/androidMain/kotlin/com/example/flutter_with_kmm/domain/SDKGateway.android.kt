package com.example.flutter_with_kmm.domain

import com.github.ln_12.library.ConnectivityStatus

actual suspend fun SDKGateway.isInternetGranted(): Boolean {
    val connectivityStatus = ConnectivityStatus(this.platform.app)
    connectivityStatus.isNetworkConnected.value
    return true
}