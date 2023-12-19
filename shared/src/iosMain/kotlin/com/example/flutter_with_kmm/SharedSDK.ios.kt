package com.example.flutter_with_kmm

import com.example.flutter_with_kmm.domain.CallHandler
import com.example.flutter_with_kmm.domain.CallbackHandler
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterError
import io.flutter.embedding.engine.FlutterMethodChannel
import io.flutter.embedding.engine.FlutterResult
import io.flutter.embedding.engine.FlutterStandardMethodCodec
import io.flutter.embedding.engine.FlutterViewController
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin
import platform.UIKit.UIApplication
import platform.posix.FALSE
import platform.posix.TRUE


internal actual fun SharedSDK.httpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient {
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
        when(result){
            is Boolean -> callResult?.invoke(if(result) "true" else "false")
            else -> callResult?.invoke(result)
        }
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