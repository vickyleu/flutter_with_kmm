package com.example.flutter_with_kmm

import android.app.Application
import com.example.flutter_with_kmm.domain.CallHandler
import com.example.flutter_with_kmm.domain.CallbackHandler
import io.flutter.app.FlutterApplication
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.plugin.common.MethodChannel
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

actual class BaseApplication(val app: Application,private val handleFlutterEngineChange:(MethodChannel)->Unit) {
    // 对外暴露的属性，通过委托实现监听
    private var flutterEngine: FlutterEngine? by AutoUpdateDelegate { newValue ->
        handleFlutterEngineChange(newValue)
    }
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

}


internal actual fun SharedSDK.httpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient {
    return HttpClient(OkHttp) {
        config()
    }
}

class CallHandlerImpl(private val callResult: MethodChannel.Result) : CallHandler {

    override fun success(result: Any?) =
        callResult.success(result)

    override fun error(errorMessage: String?) =
        callResult.error("N/A", errorMessage, null)

}

class CallbackHandlerImpl(private val methodChannel: MethodChannel) : CallbackHandler {
    override fun invokeMethod(method: String, arguments: Any?) {
        methodChannel.invokeMethod(method, arguments)
    }
}