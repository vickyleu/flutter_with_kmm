import java.util.Properties

val localProperties = Properties()
val localPropertiesFile: File = if (rootProject.file("./android").exists()) {
    rootProject.file("./android/local.properties")
} else {
    rootProject.file("local.properties")
}
val flutterVersion= latestFlutterVersion()
if (localPropertiesFile.exists()) {
    localPropertiesFile.reader(Charsets.UTF_8).use { reader ->
        localProperties.load(reader)
    }
}
val flutterRoot = localProperties.getProperty("flutter.sdk")
    ?: throw GradleException("Flutter SDK not found. Define location with flutter.sdk in the local.properties file.")
var flutterVersionCode = localProperties.getProperty("flutter.versionCode") ?: "1"
var flutterVersionName = localProperties.getProperty("flutter.versionName") ?: "1.0"



plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("dev.flutter.flutter-gradle-plugin")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
kotlin {
    jvmToolchain(17)
}

android {
    namespace = "com.example.flutter_with_kmm"
    compileSdk = FlutterExtension.getCompileSdkVersion()
    defaultConfig {
        applicationId = "com.example.flutter_with_kmm"
        minSdk = FlutterExtension.getMinSdkVersion()
        targetSdk = FlutterExtension.getTargetSdkVersion()
        versionCode = flutterVersionCode.toInt()
        versionName = flutterVersionName
        vectorDrawables.useSupportLibrary = true
        renderscriptTargetApi = FlutterExtension.getTargetSdkVersion()
        renderscriptSupportModeEnabled = true
        useLibrary("org.apache.http.legacy")
        flavorDimensions.add("default")
        multiDexEnabled = true
        ndk {
            // 排除 x86 架构
            abiFilters.apply {
                clear()
                addAll(arrayListOf("armeabi-v7a", "arm64-v8a"))
            }
        }
    }
    signingConfigs {
        create("release") {
            keyAlias = "kmm"
            keyPassword = "kmmkmm"
            storeFile = file("./kmm.jks")
            storePassword = "kmmkmm"
            enableV2Signing = true
        }
    }
    sourceSets.getByName("main") {
        this.java.srcDirs(this.java.srcDirs + file("src/main/kotlin"))
    }

    lint {
        disable.addAll(
            arrayListOf(
                "InvalidPackage",
                "TypographyFractions",
                "TypographyQuotes",
                "TrustAllX509TrustManager",
                "MissingClass"
            )
        )
        abortOnError = false
        checkReleaseBuilds = false
        checkGeneratedSources = false
        baseline = file("lint-baseline.xml")
        ignoreTestSources = true
        informational.add("StopShip")
    }
    packaging {
        resources {
            this.excludes += arrayListOf(
                "/META-INF/{AL2.0,LGPL2.1}"
            )
            this.pickFirsts += arrayListOf(
                "META-INF/native-image/okhttp/**/*"
            )
        }
        jniLibs {
            this.excludes += arrayListOf(
                "**/*/libcrypto.so"
            )
            this.pickFirsts += arrayListOf(
                "lib/**/*.so"
            )
            this.useLegacyPackaging = true
            this.keepDebugSymbols += arrayListOf(
                "**/*/libsdkcore.so", "**/*/libfin-yuvutil.so"
            )
        }
    }
    buildFeatures {
        dataBinding = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguards/proguard-rules.pro"
            )
        }
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguards/proguard-rules.pro"
            )
        }
    }
    ndkVersion = FlutterExtension.getNdkVersion()
}

extensions.configure<FlutterExtension>("flutter") {
    this.source = "./.."
}

dependencies {
//    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version")
//    implementation(libs.kotlin.coroutines)
//    implementation(libs.kotlinx.coroutines.android)
    implementation(project(":shared"))
    //noinspection UseTomlInstead
    compileOnly("io.flutter:flutter_embedding_debug:$flutterVersion")
}