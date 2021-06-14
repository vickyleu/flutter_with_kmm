package com.example.flutter_with_kmm.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(

        @SerialName("picture")
        var picture: String,

        @SerialName("thumbnail")
        var thumbnail: String,

        @SerialName("gender")
        var gender: String,

        @SerialName("firstName")
        var firstName: String,

        @SerialName("lastName")
        var lastName: String,

)
