@file:Suppress("OPT_IN_USAGE")

import org.jetbrains.kotlin.gradle.plugin.mpp.BitcodeEmbeddingMode
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.jetbrains.kotlin.gradle.targets.native.tasks.PodGenTask

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
//    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.cocoapods)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlin.atomicfu)
}

kotlin {
    jvmToolchain(17)
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    val iosTargets = listOf(iosArm64(), iosX64(), iosSimulatorArm64())
    iosTargets.forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }
//    iosArm64()
//    iosX64()
//    iosSimulatorArm64()
    applyDefaultHierarchyTemplate() // this one

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "12.0"
        podfile = project.file("../ios/Podfile")
        framework {
            baseName = "shared"
            isStatic = false
            transitiveExport = false
//            export("com.example.kmm:sharedmodule:0.0.1")
            embedBitcode(BitcodeEmbeddingMode.DISABLE)
        }
//        pod("TXIMSDK_Plus_iOS")
        // xcframework 无法正常导入
        pod("TXIMSDK_Plus_iOS_XCFramework") {
            version = libs.versions.tencent.imsdk.get()
            packageName = "ImSDK_Plus" // 定义导出的kotlin包名,不写就会变成cocoapods.${moduleName}.xxx
            // 这个moduleName一定要和 framework 的名称一致，或者说与 def 里的一致，不然，无法正确的完成 cinterop
            moduleName = "ImSDK_Plus" // 参考/build/shared/cocoapods/defs/里面的modules名称,没有后缀
            // XCFramework 无法找到,需要手动指定路径,
            val xcFrameworkPathDir =
                project.layout.buildDirectory.get().asFile.resolve("shared/cocoapods/synthetic/ios/Pods/TXIMSDK_Plus_iOS_XCFramework/ImSDK_Plus.xcframework")
            // 在构件时判断架构类型,然后自动添加
//            var breakCondition=false
//            println("project.properties::${project.properties.map {"key:"+ it.key + "\n " /*+"value:" + it.value*/ }}   targetPlatform: ${project.properties["targetPlatform"]}")
          /* val  xcFrameworkPath =  if (project.properties["targetPlatform"] == "iosArm64") {
               xcFrameworkPathDir.resolve("ios-arm64_armv7/ImSDK_Plus.framework")
            } else if (project.properties["targetPlatform"] == "iosX64") {
               xcFrameworkPathDir.resolve("ios-arm64_x86_64-maccatalyst/ImSDK_Plus.framework")
            } else if (project.properties["targetPlatform"] == "iosSimulatorArm64") {
               xcFrameworkPathDir.resolve("ios-x86_64-simulator/ImSDK_Plus.framework")
            } else {
               breakCondition=true
               null
            }.let {
                println("xcFrameworkPath: $it")
               it
            }
            if (breakCondition){

            }*/

            extraOpts = listOf(
                "-compiler-option", "-DNS_FORMAT_ARGUMENT(A)=",
                "-verbose"
            )
        }

//        extraSpecAttributes["libraries"] = "'c++', 'sqlite3'" //导入系统库
        extraSpecAttributes["resources"] =
            "['src/commonMain/resources/**', 'src/iosMain/resources/**']"
    }

    sourceSets {
        commonMain.dependencies {
            dependencies {
                implementation(libs.kotlin.coroutines)
                implementation(libs.kotlin.serialization.core)
                implementation(libs.kotlin.serialization.json)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.json)
                implementation(libs.ktor.client.serialization)
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines.extensions)

            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {
            dependencies {
                implementation(libs.ktor.client.android)
                implementation(libs.sqldelight.android.driver)
                api(libs.tencent.imsdk)
            }
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }

        all {
            languageSettings.apply {
                // Required for CPointer etc. since Kotlin 1.9.
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
            }
        }
    }
    compilerOptions {
        freeCompilerArgs.addAll(listOf("-opt-in=kotlin.RequiresOptIn", "-Xexpect-actual-classes"))
    }
}
tasks.withType<PodGenTask>().configureEach {
    doLast {
        podfile.get().apply {
            val text = readText()
            val result = text.replace(
                "post_install do |installer|",
                """post_install do |installer|
   installer.generated_projects.each do |project|
     project.targets.each do |target|
          target.build_configurations.each do |config|
              config.build_settings['IPHONEOS_DEPLOYMENT_TARGET'] = '12.0'
          end
     end
   end
   installer.pods_project.targets.each do |target|
     target.build_configurations.each do |config|
       if config.base_configuration_reference
         config.build_settings['IPHONEOS_DEPLOYMENT_TARGET'] = '12.0'
         config.build_settings['EXCLUDED_ARCHS[sdk=iphonesimulator*]'] = "i386"
       end
       xcconfig_path = config.base_configuration_reference.real_path
       xcconfig = File.read(xcconfig_path)
       xcconfig_mod = xcconfig.gsub(/DT_TOOLCHAIN_DIR/, "TOOLCHAIN_DIR")
       File.open(xcconfig_path, "w") { |file| file << xcconfig_mod }
     end
   end
""".trimIndent()
            )
            writeText(result)
        }
    }
}
android {
    compileSdk = 34
    namespace = "com.example.kmmsharedmodule"
    defaultConfig {
        minSdk = 25
    }
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
}

// Configure a Gradle plugin
sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.example.flutter_with_kmm.shared.db")
        }
    }
}
