@file:Suppress("UnstableApiUsage")

import groovy.json.JsonSlurper
import org.gradle.internal.management.DefaultDependencyResolutionManagement
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

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
    var sdkPathExists = false

    fun applyFlutterPatch(): String {
        data class FlutterPatch(
            val title: String,
            val content: String,
            val sourceLocation: String,
            val patchLocation: String,
            val description: String,
            val version: String,
            val mustBeModified: Boolean = true
        )
        val properties = java.util.Properties()
        file("local.properties").inputStream().use { properties.load(it) }
        val flutterSdkPath = properties.getProperty("flutter.sdk")
        assert(flutterSdkPath != null) {
            "flutter.sdk not set in local.properties"
        }
        //需要兼容Windows,macos,linux
        //should absolute path by xcode analyze,https://github.com/flutter/flutter/issues/110069#issuecomment-1223963568
        val flutterVersionCommand =
            if (System.getProperty("os.name").lowercase(java.util.Locale.getDefault())
                    .contains("windows")
            ) {
                "cmd /c ${flutterSdkPath}\\bin\\flutter --version"
            } else {
                "${flutterSdkPath}/bin/flutter --version"
            }
        // 在gradle kts中执行这段命令,然后的得到结果
        val flutterVersionResult =
            ProcessBuilder(flutterVersionCommand.split(" ")).start().inputStream.bufferedReader()
                .readText()
        // 通过正则表达式获取flutter的版本号
        val flutterVersion =
            Regex("Flutter\\s+(\\d+\\.\\d+\\.\\d+)").find(flutterVersionResult)?.groupValues?.get(1)
                ?: ""
        // 判断版本号是否正常获取了
        if (flutterVersion.isBlank()) {
            throw RuntimeException("flutter version cannot be obtained normally")
        }
        println("flutterVersion:$flutterVersion")


        val patches = listOf(
            FlutterPatch(
                title = "Fix Kotlin DSL bug",
                content = "由于Flutter官方存在kotlin DSL解析的bug",
                sourceLocation = "packages/flutter_tools/lib/src",
                patchLocation = "project.dart",
                description = "给官方提过issue,但是它们不愿意改 https://github.com/flutter/flutter/issues/134721\n否则当前项目无法正常被编译",
                version = flutterVersion
            ),
            FlutterPatch(
                title = "Fix KMM parse error",
                content = "Flutter是固定的取Android构建目录的上层目录去生成build下的编译文件,估计是来源于dart,\n这里需要替换掉dart的main方法,把globals.localFileSystem的编译目录重新定位到当前KMM中的build目录下",
                sourceLocation = "packages/flutter_tools/lib",
                patchLocation = "executable.dart",
                description = "否则当前项目无法正常被编译",
                version = flutterVersion
            ),
            FlutterPatch(
                title = "Fix KMM parse error",
                content = "Flutter是固定的取Android构建目录的上层目录去生成build下的编译文件,估计是来源于dart,\n这里需要替换掉dart的main方法,把globals.localFileSystem的编译目录重新定位到当前KMM中的build目录下",
                sourceLocation = "packages/flutter_tools/lib/src/android",
                patchLocation = "gradle.dart",
                description = "否则当前项目无法正常被编译",
                version = flutterVersion
            ),
            FlutterPatch(
                title = "Fix agp plugin too high error",
                content = "Flutter默认最大支持AGP8.1,强制替换成最大支持8.3",
                sourceLocation = "packages/flutter_tools/lib/src/android",
                patchLocation = "gradle_utils.dart",
                description = "否则当前项目无法正常被编译",
                version = flutterVersion
            ),
            FlutterPatch(
                title = "Fix methodChannel loading error",
                content = "Flutter里面原生插件固定目录导致加载不到,需要替换",
                sourceLocation = "packages/flutter_tools/gradle/src/main/groovy",
                patchLocation = "flutter.groovy",
                description = "如果使用了原生插件就必须修改,否则GeneratedPluginRegistrant会找不到依赖",
                version = flutterVersion,
                mustBeModified = false
            )
        )
        // Load and include Flutter plugins.
        val pluginsFile = file(".flutter-plugins-dependencies")
        fun setupFlutterPatch(flutterSdkPath: String) {
            patches.forEach {
                val replaceFile = file("patch/${it.version}/${it.patchLocation}")
                val conflictDartSource =
                    file("${flutterSdkPath}/${it.sourceLocation}/${it.patchLocation}")
                if (!replaceFile.exists() && it.mustBeModified) {
                    throw Exception("${replaceFile.absolutePath} 文件是不能删除的!!!!")
                }
                if (conflictDartSource.exists()) {
                    val text = conflictDartSource.readText()
                        .trimIndent()
                        .replace("\r\n", "")
                        .replace("\n", "")
                        .replace("\t", "")
                        .replace(" ", "")

                    val exception = Exception(
                        """
//////////////////////////// ${it.title}  ////////////////////////////
                    ${it.content}
                    请将 ===>> file://${conflictDartSource.absolutePath} <<=== 源文件
                    替换成 ${replaceFile.absolutePath} ,

                    然后执行如下命令生成新的源码快照
                    ```
                    rm ${flutterSdkPath}/bin/cache/flutter_tools.stamp & \
                    rm ${flutterSdkPath}/bin/cache/flutter_tools.snapshot & \
                    flutter pub get

                    ```
                    ${it.description}

                """.trimIndent()
                    )
                    val textReplace = replaceFile.readText()
                        .trimIndent()
                        .replace("\r\n", "\n")
                        .replace("\n", "")
                        .replace("\t", "")
                        .replace(" ", "")

                    if (textReplace != text && it.mustBeModified) {
                        throw exception
                    }else if(it.patchLocation=="flutter.groovy"&&!pluginsFile.exists()){
                        throw exception
                    }
                }
            }
        }
        setupFlutterPatch(flutterSdkPath)
        /**
        rm /Volumes/Extra/flutter/bin/cache/flutter_tools.stamp & \
        rm /Volumes/Extra/flutter/bin/cache/flutter_tools.snapshot & \
        flutter pub get
         */

        //flutterSdkPath 返回的是flutter的目录(/Volumes/Extra/flutter),下面通过命令行获取flutter的版本号
        println("flutterSdkPath:$flutterSdkPath")
        settings.extra["flutterSdkPath"] = flutterSdkPath
        includeBuild("${flutterSdkPath}/packages/flutter_tools/gradle")
        sdkPathExists=true
        return flutterSdkPath
    }
    applyFlutterPatch()
    plugins {
        if (sdkPathExists) {
            id("dev.flutter.flutter-plugin-loader") version "1.0.0" // settings plugin, only work for settings.gradle
        }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}
dependencyResolutionManagement {
    val management = this as DefaultDependencyResolutionManagement
    val repositoriesMode = management.repositoriesMode
    // 需要判断
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        // flutter
        maven { url = uri("https://storage.googleapis.com/download.flutter.io") }
        maven { url = uri("https://jitpack.io") }
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-dev") }
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
        // workaround for https://youtrack.jetbrains.com/issue/KT-51379
        exclusiveContent {
            forRepository {
                ivy("https://download.jetbrains.com/kotlin/native/builds") {
                    name = "Kotlin Native"
                    patternLayout {

                        // example download URLs:
                        // https://download.jetbrains.com/kotlin/native/builds/releases/1.7.20/linux-x86_64/kotlin-native-prebuilt-linux-x86_64-1.7.20.tar.gz
                        // https://download.jetbrains.com/kotlin/native/builds/releases/1.7.20/windows-x86_64/kotlin-native-prebuilt-windows-x86_64-1.7.20.zip
                        // https://download.jetbrains.com/kotlin/native/builds/releases/1.7.20/macos-x86_64/kotlin-native-prebuilt-macos-x86_64-1.7.20.tar.gz
                        listOf(
                                "macos-x86_64",
                                "macos-aarch64",
                                "osx-x86_64",
                                "osx-aarch64",
                                "linux-aarch64",
                                "linux-x86_64",
                                "windows-x86_64",
                        ).forEach { os ->
                            listOf("dev", "releases").forEach { stage ->
                                artifact("$stage/[revision]/$os/[artifact]-[revision].[ext]")
                            }
                        }
                    }
                    metadataSources { artifact() }
                }
            }
            filter { includeModuleByRegex(".*", ".*kotlin-native-prebuilt.*") }
        }
    }
}

rootProject.name = "flutter_with_kmm"
include(":shared")
include(":androidApp")


gradle.beforeProject {
    if (this.hasProperty("target-platform")) {
        this.setProperty(
            "target-platform",
            "android-arm,android-arm64"
        )//,android-arm64  //flutter打包记得开启,flutter engine 动态构建属性,在纯Android模式下会报错
    }
}
// Load and include Flutter plugins.
val flutterProjectRoot: Path = rootProject.projectDir.toPath()
val pluginsFile: Path = flutterProjectRoot.resolve(".flutter-plugins-dependencies")

if (Files.exists(pluginsFile)) {
    @Suppress("UNCHECKED_CAST")
    val map = JsonSlurper().parseText(
        pluginsFile.toFile().readText()
    ) as? Map<String, Map<String, List<Map<String, Any>>>>

    val pluginDirs = (map?.get("plugins")?.get("android") ?: arrayListOf()).mapNotNull {
        val name = it["name"] as String
        val path = it["path"] as String
        val needsBuild: Boolean =
            it["native_build"].let { it?.toString()?.toBooleanStrict() ?: true }
        if (!needsBuild) {
            return@mapNotNull null
        }
        val pluginDirectory = file(path).toPath().resolve("android")
        if(pluginDirectory.exists()){
           return@mapNotNull name to pluginDirectory.toFile().absolutePath
        }
        return@mapNotNull null
    }
    pluginDirs.forEachIndexed { index, t ->
        val pluginName = t.first
        val pluginPath = t.second
        include(":${pluginName}")
        val childProject = project(":${pluginName}")
        childProject.projectDir = file(pluginPath)
        rootProject.children.add(childProject)
    }
}