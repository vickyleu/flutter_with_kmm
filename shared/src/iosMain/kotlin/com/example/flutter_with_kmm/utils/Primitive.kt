package com.example.flutter_with_kmm.utils

import kotlinx.cinterop.convert
import platform.darwin.NSInteger
import platform.darwin.NSUInteger

fun Int.toNSInt():NSInteger{
    return this.convert<NSInteger>()
}
fun Int.toNSUInt():NSUInteger{
    return this.toUInt().toNSUInt()
}
fun UInt.toNSUInt():NSUInteger{
    return this.convert<NSUInteger>()
}