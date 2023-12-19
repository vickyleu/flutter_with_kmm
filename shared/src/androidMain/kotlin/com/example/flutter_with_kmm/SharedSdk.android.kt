package com.example.flutter_with_kmm

import com.example.flutter_with_kmm.domain.CallHandler
import com.example.flutter_with_kmm.domain.CallbackHandler
import io.flutter.plugin.common.MethodChannel
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp

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