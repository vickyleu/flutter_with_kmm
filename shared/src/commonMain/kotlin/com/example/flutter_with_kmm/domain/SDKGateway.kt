package com.example.flutter_with_kmm.domain

import com.example.flutter_with_kmm.BaseApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

expect suspend fun SDKGateway.isInternetGranted(): Boolean

class SDKGateway(private val interactor: SharedInteractor, internal val platform: BaseApplication) {

    internal val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun processCall(method: String, arguments: Any?, callHandler: CallHandler) {

        platform.logger.error { "processCall method:::$method" }

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
//                        this@SDKGateway.platform.logger.error { "第一次请求的时长:${}" }
                        val isGranted = isInternetGranted()
                        withContext(Dispatchers.Main) {
                            callHandler.success(isGranted)
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
    }

    fun destroy() {
        scope.cancel()
        interactor.destroy()
    }

}