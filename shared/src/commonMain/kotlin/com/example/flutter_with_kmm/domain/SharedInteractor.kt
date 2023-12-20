package com.example.flutter_with_kmm.domain

import com.example.flutter_with_kmm.entities.User
import kotlinx.coroutines.flow.Flow

interface SharedInteractor {

    fun setUsersUpdatesListener(listener: (String) -> Unit)

    fun setNativeCallbackListener(listener: (String, Map<String, Any>) -> Unit)

    suspend fun users(page: Int, results: Int): String

    suspend fun saveUser(userJson: String)

    fun getUserFlow(): Flow<List<User>>

    fun destroy()

    suspend fun callBridge(method: String, argument: Map<String, Any>)

}