package com.example.flutter_with_kmm

import com.tencent.imsdk.v2.V2TIMLogListener
import com.tencent.imsdk.v2.V2TIMManager
import com.tencent.imsdk.v2.V2TIMReceiveMessageOptInfo

import com.tencent.imsdk.v2.V2TIMSDKConfig
import com.tencent.imsdk.v2.V2TIMSDKListener
import com.tencent.imsdk.v2.V2TIMUserFullInfo
import com.tencent.imsdk.v2.V2TIMUserStatus

actual class TmSDKUtilities: V2TIMSDKListener() {
    actual fun initSdk() {
        // 初始化 config 对象
        val config = V2TIMSDKConfig()
        // 指定 log 输出级别
        config.logLevel = V2TIMSDKConfig.V2TIM_LOG_INFO
        // 指定 log 监听器
        config.logListener = object : V2TIMLogListener() {
            override fun onLog(logLevel: Int, logContent: String) {
                // logContent 为 SDK 日志内容
            }
        }
    }
    actual fun bindListener() {
        // sdkListener 类型为 V2TIMSDKListener
        V2TIMManager.getInstance().addIMSDKListener(this)
    }

    override fun onConnecting() {
    }

    override fun onConnectSuccess() {
    }

    override fun onConnectFailed(code: Int, error: String?) {
    }

    override fun onKickedOffline() {
    }

    override fun onUserSigExpired() {
    }

    override fun onSelfInfoUpdated(info: V2TIMUserFullInfo?) {
    }

    override fun onUserStatusChanged(userStatusList: MutableList<V2TIMUserStatus>?) {
    }

    override fun onUserInfoChanged(userInfoList: MutableList<V2TIMUserFullInfo>?) {
    }

    override fun onAllReceiveMessageOptChanged(receiveMessageOptInfo: V2TIMReceiveMessageOptInfo?) {
    }

    override fun onExperimentalNotify(key: String?, param: Any?) {
    }
}