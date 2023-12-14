//package xcframework
//import org.jetbrains.kotlin.gradle.plugin.CInteropSettings
//import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
//import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBinary
//import org.jetbrains.kotlin.konan.target.Family
//import org.jetbrains.kotlin.konan.target.KonanTarget
//import java.io.File
//
///**
// * Created by Sebastian Owodzin on 02/04/2023
// */
//private val xcFrameworkInfoParser = XCFrameworkInfoParser()
//
//fun File.xcFrameworkPath(name: String) = resolve("$name.xcframework")
//
//fun KonanTarget.xcFrameworksPaths(frameworksDir: File, names: Collection<String>) =
//    names.mapNotNull { name ->
//        val xcFrameworkPath = frameworksDir.xcFrameworkPath(name)
//        val libraryDefinitions = xcFrameworkInfoParser.parse(xcFrameworkPath)
//
//        libraryDefinitions.select(
//            this,
//            if (family == Family.WATCHOS) {
//                when (this) {
//                    is KonanTarget.WATCHOS_ARM32,
//                    is KonanTarget.WATCHOS_ARM64 -> KonanTarget.IOS_ARM64
//                    is KonanTarget.WATCHOS_X64 -> KonanTarget.IOS_X64
//                    is KonanTarget.WATCHOS_SIMULATOR_ARM64 -> KonanTarget.IOS_SIMULATOR_ARM64
//                    else -> null
//                }
//            } else null
//        )?.let { xcFrameworkPath.resolve(it.libraryIdentifier) }
//    }
//
//fun NativeBinary.linkXCFrameworks(frameworksDir: File, names: Collection<String>) {
//    linkerOpts(target.konanTarget.xcFrameworksPaths(frameworksDir, names).map { "-F$it" })
//}
//
//fun CInteropSettings.configureXCFrameworks(target: KotlinNativeTarget, frameworksDir: File, names: Collection<String>) {
//    compilerOpts(
//        target.konanTarget.xcFrameworksPaths(frameworksDir, names).map { "-F$it" }
//    )
//}
//
//fun KotlinNativeTarget.linkXCFrameworks(frameworksDir: File, names: Collection<String>) {
//    binaries.all {
//        linkXCFrameworks(frameworksDir, names)
//    }
//}
//
//fun KotlinNativeTarget.setupCInteropWithXCFrameworks(name: String, frameworks: Collection<String>, frameworksDir: File) {
//    compilations.getByName("main") {
//        cinterops.create(name) {
//            configureXCFrameworks(target, frameworksDir, frameworks)
//            extraOpts = listOf("-compiler-option", "-DNS_FORMAT_ARGUMENT(A)=", "-verbose")
//        }
//    }
//    linkXCFrameworks(frameworksDir, frameworks)
//}