package com.example.flutter_with_kmm.domain

interface CallHandler{
    fun success( result : Any?  )
    fun error(errorMessage: String?)
}

interface  CallbackHandler{
    fun invokeMethod(method: String, arguments: Any?)
}
