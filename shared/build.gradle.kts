import org.jetbrains.kotlin.gradle.targets.native.tasks.GenerateArtifactPodspecTask
import org.jetbrains.kotlin.gradle.targets.native.tasks.PodGenTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.PodspecTask

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.cocoapods)

    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
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
    /*fun configureNativeTarget(): KotlinNativeTarget.() -> Unit = {
        val xcFrameworkPathDir = project.layout.buildDirectory.get()
            .asFile.resolve("cocoapods/synthetic/ios/Pods/TXIMSDK_Plus_iOS_XCFramework")
        setupCInteropWithXCFrameworks("TXIMSDK_Plus_iOS_XCFramework", listOf("ImSDK_Plus"),xcFrameworkPathDir)
    }
    iosX64(configure = configureNativeTarget())
    iosArm64(configure = configureNativeTarget())*/

    iosX64()
    iosArm64()

//    iosSimulatorArm64() // TXIMSDK_Plus_iOS 不支持虚拟机,TXIMSDK_Plus_iOS_XCFramework 又无法使用
    applyDefaultHierarchyTemplate() // this one

    metadata {
        compilations.matching {
            it.name == "iosMain" || it.name == "ios"
        }.all {
            compileTaskProvider.configure { enabled = false }
        }
    }

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "12.0"
        podfile = project.file("../ios/Podfile")
//        framework {
//            baseName = "shared"
//            isStatic = true
//            transitiveExport = false
//            embedBitcode(BitcodeEmbeddingMode.DISABLE)
//        }
        framework {
            baseName = "shared"
            isStatic = true
        }
        pod("TXIMSDK_Plus_iOS") {
            // xcframework 无法正常导入
//        pod("TXIMSDK_Plus_iOS_XCFramework") {
            version = libs.versions.tencent.imsdk.get()
            packageName = "ImSDK_Plus" // 定义导出的kotlin包名,不写就会变成cocoapods.${moduleName}.xxx
            // 这个moduleName一定要和 framework 的名称一致，或者说与 def 里的一致，不然，无法正确的完成 cinterop
            moduleName = "ImSDK_Plus" // 参考/build/shared/cocoapods/defs/里面的modules名称,没有后缀
            // XCFramework 无法找到,需要手动指定路径,

            val xcFrameworkPathDir = project.layout.buildDirectory.get()
                .asFile.resolve("cocoapods/synthetic/ios/Pods/TXIMSDK_Plus_iOS_XCFramework/ImSDK_Plus.xcframework")
            extraOpts = listOf(
                "-compiler-option", "-DNS_FORMAT_ARGUMENT(A)=",
                "-verbose",
                "-Xuser-setup-hint","<<xcframework import is  unavailable>>",
//                "-libraryPath", xcFrameworkPathDir.absolutePath
            )
//            this.source=CocoapodsExtension.CocoapodsDependency.PodLocation.Path(xcFrameworkPathDir)
        }
//        extraSpecAttributes["libraries"] = "'c++', 'sqlite3'" //导入系统库
        extraSpecAttributes["resources"] =
            "['src/commonMain/resources/**', 'src/iosMain/resources/**']"
    }

    sourceSets {
        val commonMain by getting {
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
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation(libs.sqldelight.android.driver)
                api(libs.tencent.imsdk)
            }
        }
        val iosMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.ktor.client.darwin)
                implementation(libs.sqldelight.native.driver)
            }
        }

        all {
            languageSettings.apply {
                // Required for CPointer etc. since Kotlin 1.9.
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
            }
        }
    }


    /*compilerOptions {
        freeCompilerArgs.addAll(listOf("-opt-in=kotlin.RequiresOptIn", "-Xexpect-actual-classes"))
    }*/
}
tasks.withType<PodGenTask>().configureEach {
    doFirst {
        podfile.get().apply {
            var text = readText()
            val lines = text.lines().toMutableList()
            val index = lines.indexOfFirst { it.contains("config.build_settings['CODE_SIGNING_ALLOWED']") }
            lines.add(index+1,"""
                if config.base_configuration_reference
                  config.build_settings.delete 'IPHONEOS_DEPLOYMENT_TARGET'
                  config.build_settings['EXCLUDED_ARCHS[sdk=iphonesimulator*]'] = "i386"
                end
                xcconfig_path = config.base_configuration_reference.real_path
                xcconfig = File.read(xcconfig_path)
                xcconfig_mod = xcconfig.gsub(/DT_TOOLCHAIN_DIR/, "TOOLCHAIN_DIR")
                File.open(xcconfig_path, "w") { |file| file << xcconfig_mod }
        """.trimIndent().replaceIndent("            "))
            text = lines.joinToString("\n")
            writeText(text)
        }
    }
}

tasks.withType<PodspecTask>().configureEach {
    doLast {
        this@configureEach.outputFile.apply {
            var text = readText()
            val placeHolder="\${PODS_ROOT}"
            val searchPathAppend= this@configureEach.pods.get().mapNotNull {
                "\"$placeHolder/${it.name}\""
            }.joinToString(" ")
            var lines = text.lines().toMutableList()
            var index = lines.indexOfFirst { it.contains("spec.vendored_frameworks") }
            lines[index] = "    spec.vendored_frameworks      = 'shared.framework'"
            index = lines.indexOfFirst { it.contains("spec.source") }
            lines[index] = "    spec.source                   = { :path => '../build/shared/cocoapods/framework' }"
//            lines.add(index+1,"    spec.source                   = { :path => '../build/shared/cocoapods/framework' }")

            text = lines.joinToString("\n")
            /*lines = text.lines().toMutableList()
            index = lines.indexOfFirst { it.contains("spec.pod_target_xcconfig") }
            lines.add(index+1,"        'FRAMEWORK_SEARCH_PATHS' => '\"${placeHolder}/../../build/shared/cocoapods/framework\" ',")*/

            println("result::$text")
            writeText(text)
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
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguards/proguard-rules.pro"
            )
        }
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguards/proguard-rules.pro"
            )
        }
    }
}

// Configure a Gradle plugin
sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.example.flutter_with_kmm.shared.db")
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs += listOf("-Xexpect-actual-classes", "-opt-in=kotlin.RequiresOptIn")
    }
}