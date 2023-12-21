package com.example.flutter_with_kmm.domain

import com.example.flutter_with_kmm.data.SharedRepository
import com.example.flutter_with_kmm.entities.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.lighthousegames.logging.logging

class SharedInteractorImpl(
    private val sharedRepository: SharedRepository,
    private val serializer: Json
) : SharedInteractor {

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

    override fun setNativeCallbackListener(listener: (String, Map<String, Any>) -> Unit) {
        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    sharedRepository.nativeCallbackFlow.distinctUntilChanged()
                        .collect {
                        logging("flutter with kmm Gateway").e { ":::collect  ${sharedRepository.nativeCallbackFlow.hashCode()}" }
                        withContext(Dispatchers.Main) {
                            listener(it.first, it.second)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override suspend fun callBridge(method: String, argument: Map<String, Any>) {
        withContext(Dispatchers.Default) {
            try {
                sharedRepository.nativeCallbackFlow.emit(method to argument)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}