@file:Suppress("UnstableApiUsage")

import groovy.json.JsonSlurper
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

enum class ProjectBuildMode {
    KMM, FLUTTER
}

pluginManagement {
    listOf(repositories, dependencyResolutionManagement.repositories).forEach {
        it.apply {
            google {
                content {
                    includeGroupByRegex(".*google.*")
                    includeGroupByRegex(".*android.*")
                }
            }
            mavenCentral()
            maven(url = "https://androidx.dev/storage/compose-compiler/repository")
            maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
            gradlePluginPortal {
                content {
                }
            }
            maven {
                url = uri("https://jitpack.io")
                content {
                    includeGroupByRegex("com.github.*")
                }
            }
        }
    }
    var flutterVersion =""
    fun flutterSdkPath(): String? {
        val properties = java.util.Properties()
        val buildMode = if(file("./android").exists()){
            file("android/local.properties").inputStream().use { properties.load(it) }
            1
        }else{
            file("local.properties").inputStream().use { properties.load(it) }
            2
        }
        val flutterSdkPath = properties.getProperty("flutter.sdk")
        val replaceFile = when(buildMode){
            1 -> file("./project.dart.save")
            2 -> file("../project.dart.save")
            else -> {
                throw RuntimeException("project structure error")
            }
        }
        val conflictDartSource =
            file("${flutterSdkPath}/packages/flutter_tools/lib/src/project.dart")
        if (!replaceFile.exists()) {
            throw Exception("${replaceFile.absolutePath} 文件是不能删除的!!!!")
        }
        if(conflictDartSource.exists()){
            val text =conflictDartSource.readText()
            val exception = Exception("""
                    由于Flutter官方存在kotlin DSL解析的bug,
                    请将 ===>> file://${conflictDartSource.absolutePath} <<=== 源文件
                    替换成 ${replaceFile.absolutePath} ,

                    然后执行如下命令生成新的源码快照
                    ```
                    rm ${flutterSdkPath}/bin/cache/flutter_tools.stamp & \
                    rm ${flutterSdkPath}/bin/cache/flutter_tools.snapshot & \
                    flutter pub get

                    ```
                    给官方提过issue,但是它们不愿意改 https://github.com/flutter/flutter/issues/134721

                    否则当前项目无法正常被编译

                """.trimIndent())
            if(text.contains("build.gradle.kts")  && text.contains("extension _DirectoryExtension on Directory") ){
                val textReplace = replaceFile.readText()
                if(textReplace != text){
                    throw exception
                }
            }else{
                throw exception
            }
        }
        assert(flutterSdkPath != null) {
            "flutter.sdk not set in local.properties"
        }
        return flutterSdkPath
    }
    var sdkPathExists = false
    flutterSdkPath()?.apply {
        sdkPathExists = true
        //flutterSdkPath 返回的是flutter的目录(/Volumes/Extra/flutter),下面通过命令行获取flutter的版本号
        //需要兼容Windows,macos,linux
        val flutterVersionCommand = if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            "cmd /c flutter --version"
        } else {
            "flutter --version"
        }
        // 在gradle kts中执行这段命令,然后的得到结果
        val flutterVersionResult = ProcessBuilder(flutterVersionCommand.split(" ")).start().inputStream.bufferedReader().readText()
        /**
         * flutterVersionResult:`Flutter 3.16.2 • channel stable • https://github.com/flutter/flutter.git
         * Framework • revision 9e1c857886 (8 days ago) • 2023-11-30 11:51:18 -0600
         * Engine • revision cf7a9d0800
         * Tools • Dart 3.2.2 • DevTools 2.28.3
         */
        // 通过正则表达式获取flutter的版本号
        flutterVersion = Regex("Flutter\\s+(\\d+\\.\\d+\\.\\d+)").find(flutterVersionResult)?.groupValues?.get(1) ?: ""
        // 判断版本号是否正常获取了
        if(flutterVersion.isBlank()){
            throw RuntimeException("flutter version cannot be obtained normally")
        }
        println("flutterVersion:$flutterVersion")
        settings.extra["flutterSdkPath"] = this
        includeBuild("${this}/packages/flutter_tools/gradle")
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.name == "flutter") {
                useModule("dev.flutter:flutter-gradle-plugin:$flutterVersion")
            }
        }
    }
    plugins {
        if (sdkPathExists) {
            id("dev.flutter.flutter-gradle-plugin") version (flutterVersion) apply false
        }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver") version "0.7.0"
}
toolchainManagement{
    jvm {
        javaRepositories {
            repository("foojay") {
                resolverClass.set(org.gradle.toolchains.foojay.FoojayToolchainResolver::class.java)
            }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven { url=uri("https://jitpack.io") }
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-dev") }
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
    }
}
gradle.beforeProject {
    if (this.hasProperty("target-platform")) {
        this.setProperty(
            "target-platform",
            "android-arm,android-arm64"
        )//,android-arm64  //flutter打包记得开启,flutter engine 动态构建属性,在纯Android模式下会报错
    }
}


val buildMode = if(file("./android").exists()){
    rootProject.name = "flutter_with_kmm"
    include(":shared")
    include(":android")
    project(":android").projectDir = file("./android")
    include(":android:app")
    project(":android:app").projectDir = file("./android/app")
    ProjectBuildMode.KMM
}else if (file("./app").exists()){
    rootProject.name = "android"
    include(":app")
    include(":shared")
    project(":shared").projectDir = file("../shared")
    ProjectBuildMode.FLUTTER
}else{
    throw RuntimeException("project structure error")
}
// 把

check(JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    "This project needs to be run with Java 17 or higher (found: ${JavaVersion.current()})."
}

// Load and include Flutter plugins.
val flutterProjectRoot: Path = when(buildMode){
    ProjectBuildMode.KMM -> rootProject.projectDir.toPath()
    ProjectBuildMode.FLUTTER -> rootProject.projectDir.parentFile.toPath()
}
val pluginsFile: Path = flutterProjectRoot.resolve(".flutter-plugins-dependencies")
if (!Files.exists(pluginsFile)) {
    throw GradleException("Flutter plugins file not found. Define plugins in $pluginsFile.")
}
@Suppress("UNCHECKED_CAST")
val map = JsonSlurper().parseText(
    pluginsFile.toFile().readText()
) as? Map<String, Map<String, List<Map<String, Any>>>>

val androidDirs = (map?.get("plugins")?.get("android") ?: arrayListOf())
for (androidDir in androidDirs) {
    val name = androidDir["name"] as String
    val path = androidDir["path"] as String
    val needsBuild: Boolean = androidDir["native_build"].let { it?.toString()?.toBooleanStrict() ?: true }
    if (!needsBuild) {
        continue
    }
    val pluginDirectory = file(path).toPath().resolve("android")
    assert(pluginDirectory.exists())
    include(":$name")
    project(":$name").projectDir = pluginDirectory.toFile()
}
