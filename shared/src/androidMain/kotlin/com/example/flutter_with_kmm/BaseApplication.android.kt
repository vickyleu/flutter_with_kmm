package com.example.flutter_with_kmm
import android.app.Application
import com.example.flutter_with_kmm.AutoUpdateDelegate
import com.example.flutter_with_kmm.SharedSDK
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.plugin.common.MethodChannel
import org.lighthousegames.logging.logging

actual class BaseApplication(
    val app: Application,
    private val handleFlutterEngineChange: (MethodChannel) -> Unit
) {
    actual val logger = logging("flutter with kmm Gateway")
    // 对外暴露的属性，通过委托实现监听
    var flutterEngine: FlutterEngine? by AutoUpdateDelegate { newValue ->
        handleFlutterEngineChange(newValue)
    }
        private set

    fun setupEngine(engine: FlutterEngine, lifecycle: FlutterEngine.EngineLifecycleListener) {
        engine.addEngineLifecycleListener(lifecycle)
        flutterEngine = engine
        // 缓存FlutterEngine
        FlutterEngineCache
            .getInstance()
            .put("global_engine_id", flutterEngine)
    }

    // 处理 FlutterEngine 变化的方法
    private fun handleFlutterEngineChange(engine: FlutterEngine) {
        // 在这里执行变量发生变化时的操作
        // 例如，可以在这里触发相应的操作或通知
        val binaryMessenger = engine.dartExecutor.binaryMessenger
        val methodChannel = MethodChannel(binaryMessenger, SharedSDK.CHANNEL)
        handleFlutterEngineChange.invoke(methodChannel)
    }

    fun destroy(lifecycle: FlutterEngine.EngineLifecycleListener) {
        flutterEngine?.removeEngineLifecycleListener(lifecycle)
        flutterEngine = null
    }

}
