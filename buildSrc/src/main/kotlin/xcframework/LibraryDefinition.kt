//package xcframework
//
//import org.jetbrains.kotlin.konan.target.KonanTarget
//
///**
// * Created by Sebastian Owodzin on 17/09/2022
// */
//data class LibraryDefinition(
//    val libraryIdentifier: String,
//    val libraryPath: String,
//    val supportedArchitectures: List<String>,
//    val supportedPlatform: String,
//    val supportedPlatformVariant: String?
//)
//
//val KonanTarget.isAppleFamilySimulator: Boolean
//    get() = when (this) {
//        is KonanTarget.IOS_X64,
//        is KonanTarget.IOS_SIMULATOR_ARM64,
//        is KonanTarget.TVOS_X64,
//        is KonanTarget.TVOS_SIMULATOR_ARM64,
//        is KonanTarget.WATCHOS_X64,
//        is KonanTarget.WATCHOS_SIMULATOR_ARM64 -> true
//
//        else -> false
//    }
//
//fun List<LibraryDefinition>.select(
//    target: KonanTarget,
//    forceTarget: KonanTarget? = null
//): LibraryDefinition? = if (target.family.isAppleFamily) {
//    val (platform, platformVariant, architecture) = with(
//        (forceTarget?.name ?: target.name).split("_")
//    ) {
//        when (size) {
//            3 -> this
//            2 -> listOf(first(), if (target.isAppleFamilySimulator) "simulator" else null, last())
//            else -> listOf(null, null, null)
//        }
//    }
//
//    if (platform != null && architecture != null) {
//        filter {
//            it.supportedPlatform == platform
//        }.filter {
//            platformVariant?.run { it.supportedPlatformVariant == this }
//                ?: (it.supportedPlatformVariant == null)
//        }.firstOrNull {
//            it.supportedArchitectures.contains(architecture) || (architecture == "x64" && it.supportedArchitectures.contains(
//                "x86_64"
//            ))
//        }
//    } else null
//} else null