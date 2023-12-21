package com.example.flutter_with_kmm.domain

import com.example.flutter_with_kmm.BaseApplication
import com.example.flutter_with_kmm.utils.DateTimeFormatter
import com.example.flutter_with_kmm.utils.dateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

expect suspend fun SDKGateway.isInternetGranted(): Boolean

class SDKGateway(internal val interactor: SharedInteractor, internal val platform: BaseApplication) {

    internal val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Throws(Exception::class)
    fun processCall(method: String, arguments: Any?, callHandler: CallHandler) {
        scope.launch {
            try {
                when (method) {
                    "users" -> {
                        @Suppress("UNCHECKED_CAST")
                        val args = arguments as List<Int>
                        val page = args[0]
                        val results = args[1]
                        val result = interactor.users(page, results)
                        withContext(Dispatchers.Main) {
                            callHandler.success(result)
                        }
                    }

                    "saveUser" -> {
                        val userJson = arguments as String
                        interactor.saveUser(userJson)
                        withContext(Dispatchers.Main) {
                            callHandler.success(true)
                        }
                    }

                    "isInternetGranted" -> {
                        val dtf=DateTimeFormatter("yyyy-MM-dd HH:mm:ss")
                        try {
//                            this@SDKGateway.platform.logger.error { "第一次请求的时长:${dtf.format(Clock.System.now().dateTime())}" }
                            val isGranted = isInternetGranted()
//                            this@SDKGateway.platform.logger.error { "第一次请求完成的时长:${dtf.format(Clock.System.now().dateTime())}" }
                            withContext(Dispatchers.Main) {
                                callHandler.success(isGranted)
                            }
                        }catch (e:Exception){
                            e.printStackTrace()
                            withContext(Dispatchers.Main) {
                                callHandler.error("${e.message}")
                            }
                        }

                    }

                    else -> {
                        withContext(Dispatchers.Main) {
                            callHandler.error("Method not implemented")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callHandler.error(e.message)
                }
            }
        }
    }

    fun setCallbacks(callback: CallbackHandler) {
        interactor.setUsersUpdatesListener {
            callback.invokeMethod("users", it)
        }
        interactor.setNativeCallbackListener {method,map->
            val arguments:Map<String,Any> = mapOf("method" to method, "args" to map)
            callback.invokeMethod("nativeCallback", arguments)
        }
    }

    fun destroy() {
        scope.cancel()
        interactor.destroy()
    }

}