package com.example.flutter_with_kmm.data.api.mapper

import com.example.flutter_with_kmm.entities.User
import com.example.flutter_with_kmm.data.api.entities.UserDTO
import com.example.flutter_with_kmm.data.api.entities.UsersDTO

object UserDTOMapper {

    fun UsersDTO.toUserList(): List<User> {
        return results.map { it.toUser() }
    }

    private fun UserDTO.toUser(): User{
        return User(
                picture = picture.large,
                thumbnail = picture.thumbnail,
                gender = gender,
                firstName = name.first,
                lastName = name.last
        )
    }
    
}