package com.example.flutter_with_kmm.data

import com.example.flutter_with_kmm.entities.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

interface SharedRepository {

    suspend fun users(page: Int, results: Int): List<User>

    suspend fun saveUser(user: User)

    fun getUserFlow(): Flow<List<User>>

    val nativeCallbackFlow :MutableSharedFlow<Pair<String, Map<String, Any>>>
}