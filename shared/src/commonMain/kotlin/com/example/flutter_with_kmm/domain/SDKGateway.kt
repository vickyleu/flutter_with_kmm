package com.example.flutter_with_kmm.domain

import kotlinx.coroutines.*

class SDKGateway(private val interactor: SharedInteractor) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun processCall(method : String, arguments : Any?, callHandler : CallHandler  ) {
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
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callHandler.error(e.message)
                }
            }
        }
    }

    fun setCallbacks(callback : CallbackHandler){
        interactor.setUsersUpdatesListener {
            callback.invokeMethod("users", it)
        }
    }

    fun destroy(){
        scope.cancel()
        interactor.destroy()
    }
    
}