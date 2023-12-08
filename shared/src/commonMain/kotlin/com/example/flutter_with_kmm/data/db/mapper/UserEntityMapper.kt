package com.example.flutter_with_kmm.data.db.mapper

import com.example.flutter_with_kmm.entities.User
import com.example.flutterwithkmm.shared.db.UserEntity

object UserEntityMapper {

    fun UserEntity.toUser(): User {
        return User(
            picture = picture,
            thumbnail = picture,
            gender = gender,
            firstName = firstName,
            lastName = lastName
        )
    }

    fun User.toEntity(): UserEntity {
        return UserEntity(
            picture = picture,
            thumbnail = picture,
            gender = gender,
            firstName = firstName,
            lastName = lastName
        )
    }

}