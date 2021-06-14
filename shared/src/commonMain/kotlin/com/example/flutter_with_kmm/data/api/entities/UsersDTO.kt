package com.example.flutter_with_kmm.data.api.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UsersDTO(

        @SerialName("results") 
        var results: List<UserDTO>,
        
)

@Serializable
data class UserDTO(

        @SerialName("name")
        var name: NameDTO,

        @SerialName("picture")
        var picture: PictureDTO,

        @SerialName("gender")
        var gender: String

)

@Serializable
data class NameDTO(

        @SerialName("first")
        var first: String,

        var last: String,

)

@Serializable
data class PictureDTO(

        @SerialName("large")
        var large: String,

        @SerialName("thumbnail")
        var thumbnail: String,

)