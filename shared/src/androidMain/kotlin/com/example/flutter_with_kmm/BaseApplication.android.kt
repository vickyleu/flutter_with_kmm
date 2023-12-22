package com.example.flutter_with_kmm

import android.app.Application
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.plugin.common.MethodChannel
import org.lighthousegames.logging.logging

actual class BaseApplication(
    val app: Application,
    private val handleFlutterEngineChange: (MethodChannel) -> Unit
) {
    actual val logger = logging()


    internal var networkReceiver: Any? = null

    // 对外暴露的属性，通过委托实现监听
    var flutterEngine: FlutterEngine? by AutoUpdateDelegate { newValue ->
        this.logger.error { "BaseApplication handleFlutterEngineChange: ${newValue.hashCode()}" }
        handleFlutterEngineChange(newValue)
    }
        private set

    fun setupEngine(engine: FlutterEngine, lifecycle: FlutterEngine.EngineLifecycleListener) {
        engine.addEngineLifecycleListener(lifecycle)
        flutterEngine = engine
        // 缓存FlutterEngine
        val engineCache = FlutterEngineCache.getInstance()
        if (!engineCache.contains("global_engine_id")) {
            engineCache.put("global_engine_id", engine)
        }
    }

    // 处理 FlutterEngine 变化的方法
    private fun handleFlutterEngineChange(engine: FlutterEngine) {
        // 在这里执行变量发生变化时的操作
        // 例如，可以在这里触发相应的操作或通知
        val binaryMessenger = engine.dartExecutor.binaryMessenger
        val methodChannel = MethodChannel(binaryMessenger, SharedSDK.CHANNEL)
        handleFlutterEngineChange.invoke(methodChannel)
        engine.dartExecutor.binaryMessenger.enableBufferingIncomingMessages()
    }

    fun destroy(lifecycle: FlutterEngine.EngineLifecycleListener) {
        flutterEngine?.removeEngineLifecycleListener(lifecycle)
        val engineCache = FlutterEngineCache.getInstance()
        if (engineCache.contains("global_engine_id")) {
            engineCache.remove("global_engine_id")
        }
        flutterEngine?.apply {
            dartExecutor.binaryMessenger.disableBufferingIncomingMessages()
        }
        flutterEngine = null
    }

}
