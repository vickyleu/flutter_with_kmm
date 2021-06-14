package com.example.flutter_with_kmm

import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel
import android.os.Bundle
import com.example.flutter_with_kmm.data.db.DatabaseDriverFactory
import com.example.flutter_with_kmm.domain.CallHandler
import com.example.flutter_with_kmm.domain.CallbackHandler
import com.example.flutter_with_kmm.domain.SDKGateway

private const val CHANNEL = "example/platform"

class MainActivity: FlutterActivity() {

    private val gateway: SDKGateway = SharedSDK(
            driverFactory = DatabaseDriverFactory(context),
    ).gateway

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binaryMessenger = flutterEngine?.dartExecutor?.binaryMessenger
        val methodChannel = MethodChannel(binaryMessenger, CHANNEL)
        methodChannel.setMethodCallHandler{ call, result ->
            gateway.processCall(call.method, call.arguments, CallHandlerImpl(result) )
        }
        gateway.setCallbacks(CallbackHandlerImpl(methodChannel))
    }

    override fun onDestroy() {
        super.onDestroy()
        gateway.destroy()
    }

}

class CallHandlerImpl(private val callResult : MethodChannel.Result) : CallHandler {

    override fun success(result: Any?) =
            callResult.success(result)

    override fun error(errorMessage: String?) =
            callResult.error("N/A", errorMessage, null)

}

class CallbackHandlerImpl(private val methodChannel : MethodChannel) : CallbackHandler {

    override fun invokeMethod(method: String, arguments: Any?) {
        methodChannel.invokeMethod(method, arguments)
    }

}