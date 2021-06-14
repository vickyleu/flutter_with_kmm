package com.example.flutter_with_kmm.data.db

import com.example.flutter_with_kmm.data.db.mapper.UserEntityMapper.toEntity
import com.example.flutter_with_kmm.data.db.mapper.UserEntityMapper.toUser
import com.example.flutter_with_kmm.entities.User
import com.example.flutter_with_kmm.shared.db.AppDatabase
import com.squareup.sqldelight.runtime.coroutines.asFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class UserDaoImpl(driverFactory: DatabaseDriverFactory): UserDao{

    private val userDB = AppDatabase(driverFactory.createDriver()).flutterWithKmmDbQueries

    override suspend fun saveUser(user: User) = withContext(Dispatchers.Default) {
        userDB.updateUser(user.toEntity())
    }

    override fun getUserFlow(): Flow<List<User>> = userDB.getAllUsers()
            .asFlow()
            .map { query ->
                query.executeAsList().map { it.toUser() }
            }

}