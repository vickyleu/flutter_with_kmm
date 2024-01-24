package com.example.flutter_with_kmm.domain

import com.example.flutter_with_kmm.connectivity.currentReachable
import com.example.flutter_with_kmm.connectivity.restrictedState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.UIKit.UIDevice

fun available(version: Float): Boolean {
    return available(version.toString())
}

fun available(version: String): Boolean {
    val vStr1 = UIDevice.currentDevice.systemVersion
    var maxSize: Int
    val list1 = mutableListOf<Int>()
    val list2 = mutableListOf<Int>()
    if (vStr1.contains(".")) {
        vStr1.split(".").forEach {
            list1.add(it.toIntOrNull() ?: 0)
        }
    }
    if (version.replace("f", "").replace("F", "")
            .contains(".")
    ) {
        version.split(".").forEach {
            list2.add(it.toIntOrNull() ?: 0)
        }
    }
    maxOf(list1.size, list2.size).let {
        maxSize = it
    }
    if (list1.size == maxSize) {
        repeat(maxSize - list2.size) {
            list2.add(0)
        }
    } else {
        repeat(maxSize - list1.size) {
            list1.add(0)
        }
    }
    // 得到两个数组,长度相等,现在需要比较这两个数组的版本号大小,比如第一个是1.0.5,第二个是1.0.0,那么第一个大于第二个
    list1.forEachIndexed { index, i ->
        val i1 = list2[index]
        if (i > i1) {
            println("api可用")
            return true
        } else if (i < i1) {
            println("api不可用")
            return false
        }
    }
    println("api可能可用")
    return true
}

@Throws(Exception::class)
actual suspend fun SDKGateway.isInternetGranted(): Boolean {
    try {
        if (!available(10.0f) || this.currentReachable()) {
            return true
        }
        return this.scope.value.launch {
            withContext(Dispatchers.Unconfined) {
                this@isInternetGranted.restrictedState()
            }
        }.start()
    } catch (e: Exception) {
        e.printStackTrace()
        this.platform.logger.e { "isInternetGranted: $e" }
        return false
    }
}

internal actual fun SDKGateway.platformDestroy() {
    this.platform._cellularData.cellularDataRestrictionDidUpdateNotifier = null
}