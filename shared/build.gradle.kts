import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.PodGenTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.PodspecTask
import org.jetbrains.kotlin.incremental.createDirectory

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

    val iosSupported = listOf(
        iosX64(),
        iosArm64(),
    )
    iosSupported.forEach {
       it.binaries {
              framework {
                baseName = "shared"
                isStatic = true
                export("org.lighthousegames:logging:1.3.0")
              }
       }
    }

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
        pod("Reachability", "~> 3.2")
        pod("Flutter"){
            packageName="io.flutter.embedding.engine"
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
                "-Xuser-setup-hint", "<<xcframework import is  unavailable>>",
//                "-libraryPath", xcFrameworkPathDir.absolutePath
            )
//            this.source=CocoapodsExtension.CocoapodsDependency.PodLocation.Path(xcFrameworkPathDir)
        }
        extraSpecAttributes["libraries"] = "'c++', 'sqlite3'" //导入系统库
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


                implementation(libs.settings.noarg)
                implementation("com.github.ln-12:multiplatform-connectivity-status:1.2.0")
                implementation(libs.kotlinx.datetime)
                api(libs.logging)
                implementation(libs.stately.common)


                api(kotlin("reflect"))

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
                val flutterVersion= latestFlutterVersion()
                //noinspection UseTomlInstead
                compileOnly("io.flutter:flutter_embedding_debug:$flutterVersion")
                api(libs.tencent.imsdk)
//                api("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
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
                optIn("kotlinx.cinterop.BetaInteropApi")
                optIn("kotlinx.serialization.InternalSerializationApi")
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
            }
        }
    }


    /*compilerOptions {
        freeCompilerArgs.addAll(listOf("-opt-in=kotlin.RequiresOptIn", "-Xexpect-actual-classes"))
    }*/
}

tasks.withType<PodGenTask>().configureEach {
    doLast {
        podfile.get().apply {
            CocoapodsAppender.Builder(this)
                .append(
                    "config.build_settings['CODE_SIGNING_ALLOWED']",
                    """
                if config.base_configuration_reference
                  config.build_settings.delete 'IPHONEOS_DEPLOYMENT_TARGET'
                  config.build_settings['EXCLUDED_ARCHS[sdk=iphonesimulator*]'] = "i386"
                end
                xcconfig_path = config.base_configuration_reference.real_path
                xcconfig = File.read(xcconfig_path)
                xcconfig_mod = xcconfig.gsub(/DT_TOOLCHAIN_DIR/, "TOOLCHAIN_DIR")
                File.open(xcconfig_path, "w") { |file| file << xcconfig_mod }
        """.trimIndent().replaceIndent("            ")
                )
                .build().apply {
                    writeText(this)
                }
        }
    }
}
tasks.withType<PodspecTask>().configureEach {
    doLast {
        // podspec cannot ref framework from parent dir, so we need to copy symbol link to current dir
        outputFile.apply {
            CocoapodsAppender.Builder(this)
                .replace(
                    "spec.vendored_frameworks",
                    "    spec.vendored_frameworks      = 'framework/${project.name}.framework'"
                )
                .build().apply {
                    writeText(this)
                }
        }
        val source =
            projectDir.parentFile.resolve("build/shared/cocoapods/framework/${project.name}.framework").absolutePath
        val dest = "./${project.name}.framework"
        val frameworkLinkDir = projectDir.resolve("framework")
        frameworkLinkDir.createDirectory()
        projectDir.resolve("framework/${project.name}.framework").apply {
            if(exists()){
                delete()
            }
        }
        ProcessBuilder().directory(frameworkLinkDir)
            .command(
                "ln", "-s", source, dest
            )
            .start().apply {
                waitFor()
            }.inputStream.bufferedReader()
            .readText().apply {
                println("link shared.framework result:$this")
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