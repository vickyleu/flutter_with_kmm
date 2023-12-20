package com.example.flutter_with_kmm

import com.example.flutter_with_kmm.domain.SDKNetworkType
import com.example.flutter_with_kmm.domain.getCurrentNetworkType
import com.example.flutter_with_kmm.utils.pref
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterMethodChannel
import io.flutter.embedding.engine.FlutterStandardMethodCodec
import io.flutter.embedding.engine.FlutterViewController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.lighthousegames.logging.logging
import platform.CoreFoundation.CFRunLoopGetCurrent
import platform.CoreFoundation.kCFRunLoopDefaultMode
import platform.CoreTelephony.CTCellularData
import platform.CoreTelephony.CTCellularDataRestrictedState
import platform.SystemConfiguration.SCNetworkReachabilityCreateWithName
import platform.SystemConfiguration.SCNetworkReachabilityRef
import platform.SystemConfiguration.SCNetworkReachabilityScheduleWithRunLoop
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationState
import platform.UIKit.UIDevice
import platform.darwin.TARGET_OS_SIMULATOR
import kotlin.time.Duration.Companion.seconds

actual class BaseApplication(
    val app: UIApplication,
    private val handleFlutterEngineChange: (FlutterMethodChannel) -> Unit
) {
    actual val logger = logging("flutter with kmm Gateway")
    private var flutterEngine: FlutterEngine? by AutoUpdateDelegate { newValue ->
        handleFlutterEngineChange(newValue)
    }

    var _reachabilityRef: SCNetworkReachabilityRef? = null
        private set

    var _cellularData = CTCellularData()
        private set

    @Suppress("unused")
    fun setupEngine(controller: FlutterViewController, lifecycle: EngineLifecycleListener) {
        controller.engine()?.apply {
            addEngineLifecycleListener(lifecycle, controller)
            flutterEngine = this
        }
    }

    // 处理 FlutterEngine 变化的方法
    private fun handleFlutterEngineChange(engine: FlutterEngine) {
        // 在这里执行变量发生变化时的操作
        // 例如，可以在这里触发相应的操作或通知
        logger.e { "FlutterEngine changed: $engine" }
        val binaryMessenger = engine.binaryMessenger
        val methodChannel = FlutterMethodChannel(
            name = SharedSDK.CHANNEL,
            binaryMessenger = binaryMessenger,
            codec = FlutterStandardMethodCodec.sharedInstance()
        )
        handleFlutterEngineChange.invoke(methodChannel)
    }

    @Suppress("unused")
    fun FlutterEngine.addEngineLifecycleListener(
        listener: EngineLifecycleListener,
        controller: FlutterViewController
    ) {
//        controller.addObserver(this,"", NSKeyValueObservingOptionNew,null)
    }

    internal var isFirstRun by pref(true)

    init {
        if (UIDevice.currentDevice.systemVersion.toFloat() >= 10.0 && !isSimulator()) {
            _reachabilityRef = SCNetworkReachabilityCreateWithName(null, "223.5.5.5")
            // 此句会触发系统弹出权限询问框
            SCNetworkReachabilityScheduleWithRunLoop(
                _reachabilityRef,
                CFRunLoopGetCurrent(),
                kCFRunLoopDefaultMode
            )
            /* _cellularData.setCellularDataRestrictionDidUpdateNotifier {
                 logger.e { "state:$it" }
                 if (it.ordinal == 0) {
                     logger.e { "网络权限已开启" }
                 } else if (it.ordinal == 1) {
                     logger.e {"网络权限已关闭"}
                 } else {
                     logger.e {"网络权限未知"}
                 }
             }*/


        }
    }



    internal suspend fun waitActive() {
        return withContext(Dispatchers.IO) {
            delay(3.seconds)
            return@withContext withContext(Dispatchers.Main) {
                if (app.applicationState == UIApplicationState.UIApplicationStateActive) {
                    /*  val state = _cellularData.restrictedState()
                      when(state){
                          CTCellularDataRestrictedState.kCTCellularDataNotRestricted -> TODO()
                          CTCellularDataRestrictedState.kCTCellularDataRestrictedStateUnknown -> TODO()
                          CTCellularDataRestrictedState.kCTCellularDataRestricted -> TODO()
                          else -> TODO()
                      }*/
                    logger.error { "网络已连接" }
                } else {
                    logger.error { "网络异常" }
                }
            }
        }
    }

    /// 是否是模拟器
    internal fun isSimulator(): Boolean {
        return TARGET_OS_SIMULATOR == 1
    }


}
