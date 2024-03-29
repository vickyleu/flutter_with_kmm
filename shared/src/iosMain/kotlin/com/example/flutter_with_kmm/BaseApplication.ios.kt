package com.example.flutter_with_kmm

import com.example.flutter_with_kmm.domain.available
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
import platform.SystemConfiguration.SCNetworkReachabilityCreateWithName
import platform.SystemConfiguration.SCNetworkReachabilityRef
import platform.SystemConfiguration.SCNetworkReachabilityScheduleWithRunLoop
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationState
import platform.darwin.TARGET_OS_SIMULATOR
import kotlin.time.Duration.Companion.seconds

actual class BaseApplication(
    // IMPORTANT app必须在主线程访问,不然会阻塞主线程上正在查询的协程,进而导致主线程等待,导致页面卡住
    val app: UIApplication,
    private val handleFlutterEngineChange: (FlutterMethodChannel) -> Unit
) {
    var isFlutterEngineReady = false
    actual val logger = logging()
    private var flutterEngine: FlutterEngine? by AutoUpdateDelegate { newValue ->
        handleFlutterEngineChange(newValue)
    }

    internal var _reachabilityRef: SCNetworkReachabilityRef? = null
        private set

    internal var _cellularData = CTCellularData()
        private set


    @Suppress("unused")
    fun setupEngine(controller: FlutterViewController, lifecycle: EngineLifecycleListener) {
        controller.engine()?.apply {
            addEngineLifecycleListener(lifecycle, controller)
            flutterEngine = this
            isFlutterEngineReady = true
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
        @Suppress("unused")
        listener: EngineLifecycleListener,
        @Suppress("unused")
        controller: FlutterViewController
    ) {
//        controller.addObserver(this,"", NSKeyValueObservingOptionNew,null)
    }

    internal var isFirstRun by pref(true)

    init {
        if (available(10.0f) && !isSimulator()) {
            _reachabilityRef = SCNetworkReachabilityCreateWithName(null, "223.5.5.5")
            // 此句会触发系统弹出权限询问框
            SCNetworkReachabilityScheduleWithRunLoop(
                _reachabilityRef,
                CFRunLoopGetCurrent(),
                kCFRunLoopDefaultMode
            )
        }
    }


    internal suspend fun waitActive() {
        withContext(Dispatchers.IO) {
            delay(3.seconds)
            withContext(Dispatchers.Main) {
                if (app.applicationState == UIApplicationState.UIApplicationStateActive) {
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

    actual fun isHotRestart(): Boolean {
        return isFlutterEngineReady
    }

}
