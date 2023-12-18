package com.example.flutter_with_kmm

import com.example.flutter_with_kmm.domain.CallHandler
import com.example.flutter_with_kmm.domain.CallbackHandler
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterError
import io.flutter.embedding.engine.FlutterMethodChannel
import io.flutter.embedding.engine.FlutterResult
import io.flutter.embedding.engine.FlutterStandardMessageCodec
import io.flutter.embedding.engine.FlutterViewController
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin
import platform.UIKit.UIApplication

actual class BaseApplication(
    val app: UIApplication,
    private val handleFlutterEngineChange: (FlutterMethodChannel) -> Unit
) {
    private var flutterEngine: FlutterEngine? by AutoUpdateDelegate { newValue ->
        handleFlutterEngineChange(newValue)
    }

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
        val binaryMessenger = engine.binaryMessenger
        val methodChannel = FlutterMethodChannel(
            name = SharedSDK.CHANNEL, binaryMessenger = binaryMessenger,
            codec = FlutterStandardMessageCodec.sharedInstance()
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
}

internal actual fun httpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient {
    return HttpClient(Darwin) {
        config()
    }
}

interface EngineLifecycleListener {
    @Suppress("unused")
    fun onPreEngineRestart()
    @Suppress("unused")
    fun onEngineWillDestroy()
}

@Suppress("unused")
class CallHandlerImpl(private val callResult: FlutterResult) : CallHandler {

    override fun success(result: Any?) {
        callResult?.invoke(result)
    }

    override fun error(errorMessage: String?) {
        val error = FlutterError.errorWithCode("N/A", errorMessage, null)
        callResult?.invoke(error)
    }

}

@Suppress("unused")
class CallbackHandlerImpl(private val methodChannel: FlutterMethodChannel) : CallbackHandler {
    override fun invokeMethod(method: String, arguments: Any?) {
        try {
            methodChannel.invokeMethod(method, arguments)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}