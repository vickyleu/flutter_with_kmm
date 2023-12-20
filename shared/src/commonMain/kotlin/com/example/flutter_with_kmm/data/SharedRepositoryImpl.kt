package com.example.flutter_with_kmm.data

import com.example.flutter_with_kmm.data.api.UserApi
import com.example.flutter_with_kmm.data.api.mapper.UserDTOMapper.toUserList
import com.example.flutter_with_kmm.data.db.UserDao
import com.example.flutter_with_kmm.entities.User
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class SharedRepositoryImpl(
    private val userApi: UserApi,
    private val userDao: UserDao
) : SharedRepository {

    override suspend fun users(page: Int, results: Int): List<User> =
        userApi.users(page, results).toUserList()

    override suspend fun saveUser(user: User) = userDao.saveUser(user)

    override fun getUserFlow(): Flow<List<User>> = userDao.getUserFlow()
    override val nativeCallbackFlow: MutableSharedFlow<Pair<String, Map<String, Any>>>
        get() = MutableSharedFlow(replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

}