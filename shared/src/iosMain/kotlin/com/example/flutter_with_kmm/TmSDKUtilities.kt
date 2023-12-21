package com.example.flutter_with_kmm

import ImSDK_Plus.V2TIMManager
import ImSDK_Plus.V2TIMReceiveMessageOptInfo
import ImSDK_Plus.V2TIMSDKConfig
import ImSDK_Plus.V2TIMSDKListenerProtocol
import ImSDK_Plus.V2TIMUserFullInfo
import ImSDK_Plus.V2TIM_LOG_INFO
import platform.darwin.NSObject

actual class TmSDKUtilities : NSObject(), V2TIMSDKListenerProtocol {
    actual fun initSdk() {
        // 初始化 config 对象
        val config = V2TIMSDKConfig()
        // 指定 log 输出级别
        config.logLevel = V2TIM_LOG_INFO
        // 设置 log 监听器
        @Suppress("unused")
        config.logListener = { logLevel, logContent ->
            // logContent 为 SDK 日志内容
        }
    }

    actual fun bindListener() {
        // self 类型为 id<V2TIMSDKListener>
        V2TIMManager.sharedInstance()?.addIMSDKListener(this)
    }

    override fun onAllReceiveMessageOptChanged(receiveMessageOptInfo: V2TIMReceiveMessageOptInfo?) {
    }

    override fun onConnectFailed(code: Int, err: String?) {
    }

    override fun onConnectSuccess() {
    }

    override fun onConnecting() {
    }

    override fun onExperimentalNotify(key: String?, param: NSObject?) {
    }

    override fun onKickedOffline() {
    }

    // 关闭编译器参数名必须和父类名称匹配的警告,因为父类是Info,而这里是info,不符合代码规范
    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun onSelfInfoUpdated(info: V2TIMUserFullInfo?) {
    }

    override fun onUserInfoChanged(userInfoList: List<*>?) {
    }

    override fun onUserSigExpired() {
    }

    override fun onUserStatusChanged(userStatusList: List<*>?) {
    }
}