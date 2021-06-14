package com.example.flutter_with_kmm.domain

import com.example.flutter_with_kmm.data.SharedRepository
import com.example.flutter_with_kmm.entities.User
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SharedInteractorImpl constructor(
        private val sharedRepository: SharedRepository, 
        private val serializer: Json): SharedInteractor {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun setUsersUpdatesListener(listener: (String) -> Unit) {
        scope.launch {
            sharedRepository.getUserFlow().collect {
                withContext(Dispatchers.Main) {
                    listener(serializer.encodeToString(it))
                }
            }
        }
    }

    override suspend fun users(page: Int, results: Int): String =
            serializer.encodeToString(sharedRepository.users(page, results))

    override suspend fun saveUser(userJson: String) =
            sharedRepository.saveUser(serializer.decodeFromString(userJson))

    override fun getUserFlow(): Flow<List<User>> = sharedRepository.getUserFlow()

    override fun destroy() {
        scope.cancel()
    }

}