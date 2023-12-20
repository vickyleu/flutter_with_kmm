package com.example.flutter_with_kmm.domain

const val InternetGranted = "InternetGranted"
const val GrantedType = "Granted"


enum class SDKNetworkGrantedType {
    Accessible,
    Restricted,
    Unknown
}

enum class SDKNetworkType {
    WIFI,
    Cellular,
    Offline,
    Unknown
}