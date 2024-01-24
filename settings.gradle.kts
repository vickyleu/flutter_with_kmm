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

        data class Bilingual(val chn: String, val eng: String)
        // 以下纠正
        operator fun String.rem(other: String): Bilingual {
            return Bilingual(this, other)
        }

        data class FlutterPatch(
            val title: Bilingual,
            val content: Bilingual,
            val sourceLocation: String,
            val patchLocation: String,
            val description: Bilingual,
//            val version: String,// no need
            val mustBeModified: Boolean = true,
            val alert: Bilingual? = null,
        )
        val properties = java.util.Properties()
        file("local.properties").inputStream().use { properties.load(it) }
        var flutterSdkPath = properties.getProperty("flutter.sdk")
        if(flutterSdkPath.isNullOrBlank()){
           ProcessBuilder("flutter pub get".split(" ")).start().inputStream.bufferedReader()
                    .readText()
            file("local.properties").inputStream().use { properties.load(it) }
            flutterSdkPath = properties.getProperty("flutter.sdk")
        }
        if(flutterSdkPath.isNullOrBlank()){
            throw RuntimeException("flutter.sdk not set in local.properties, please run \"flutter pub get\" first")
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
                title = "修复Kts解析" % "Fix Kotlin DSL detection error",
                content = "由于Flutter官方存在kotlin DSL解析的bug" % "There is a bug in the official kotlin DSL parsing",
                sourceLocation = "packages/flutter_tools/lib/src",
                patchLocation = "project.dart",
                description =
                ("给官方提过issue,但是它们不愿意改 https://github.com/flutter/flutter/issues/134721\n" +
                        "否则当前项目无法正常被编译") % "I have given the official issue https://github.com/flutter/flutter/issues/134721\n but they are unwilling to change",
//                version = flutterVersion
            ),
            FlutterPatch(
                title = "修复KMM项目结构解析错误" % "Fix KMM project structure parsing error",
                content = ("Flutter是固定的取Android构建目录的上层目录去生成build下的编译文件,估计是来源于dart,\n这里需要替换掉dart的main方法,把globals.localFileSystem的编译目录重新定位到当前KMM中的build目录下")
                % ("Flutter is fixed to take the upper directory of the Android build directory to generate the compilation file under build, which is estimated to come from dart,\n" +
                        "Here you need to replace the main method of dart, and reposition the compilation directory of globals.localFileSystem to the build directory of the current KMM"),
                sourceLocation = "packages/flutter_tools/lib",
                patchLocation = "executable.dart",
                description = "否则当前项目无法正常被编译" % "Otherwise, the current project cannot be compiled normally",
//                version = flutterVersion
            ),
            FlutterPatch(
                title = "修复KMM项目结构解析错误" % "Fix KMM project structure parsing error",
                content = ("Flutter是固定的取Android构建目录的上层目录去生成build下的编译文件,估计是来源于dart,\n这里需要替换掉dart的main方法,把globals.localFileSystem的编译目录重新定位到当前KMM中的build目录下")
                        % ("Flutter is fixed to take the upper directory of the Android build directory to generate the compilation file under build, which is estimated to come from dart,\n" +
                        "Here you need to replace the main method of dart, and reposition the compilation directory of globals.localFileSystem to the build directory of the current KMM"),
                sourceLocation = "packages/flutter_tools/lib/src/android",
                patchLocation = "gradle.dart",
                description = "否则当前项目无法正常被编译" % "Otherwise, the current project cannot be compiled normally",
//                version = flutterVersion
            ),
            FlutterPatch(
                title = "修复AGP插件过高错误" % "Fix AGP plugin too high error",
                content = "Flutter默认最大支持AGP8.1,强制替换成最大支持8.3" % "Flutter defaults to a maximum support for AGP8.1, and forcibly replaces it with a maximum support for 8.3",
                sourceLocation = "packages/flutter_tools/lib/src/android",
                patchLocation = "gradle_utils.dart",
                description = "否则当前项目无法正常被编译" % "Otherwise, the current project cannot be compiled normally",
//                version = flutterVersion
            ),
            FlutterPatch(
                title = "修复methodChannel加载错误" % "Fix methodChannel loading error",
                content = "Flutter里面原生插件固定目录导致加载不到,需要替换" % "The fixed directory of the native plugin in Flutter causes it to fail to load and needs to be replaced",
                sourceLocation = "packages/flutter_tools/gradle/src/main/groovy",
                patchLocation = "flutter.groovy",
                description = "如果使用了原生插件就必须修改,否则GeneratedPluginRegistrant会找不到依赖" % "If you use native plugins, you must modify them, otherwise GeneratedPluginRegistrant will not find the dependencies",
//                version = flutterVersion,
                mustBeModified = false,
                alert = "请小心,你的flutter.groovy文件中包含了@java.util.Optional注解,是由于复制代码时被系统自动修改的,脚本会尝试自动修复" % "Please be careful, your flutter.groovy file contains the @java.util.Optional annotation, which is automatically modified by the system when copying code, and the script will try to fix it automatically"
            )
        )
        // Load and include Flutter plugins.
        val pluginsFile = file(".flutter-plugins-dependencies")
        fun checkSource(
            conflictDartSource: File,
            it: FlutterPatch,
            replaceFile: File,
            flutterSdkPath: String
        ) {
            val text = conflictDartSource.readText()
                .trimIndent()
                .replace("\r\n", "")
                .replace("\n", "")
                .replace("\t", "")
                .replace(" ", "")

            // 获得当前语言环境是否是中文,非中文就是英文
            val isChinese = java.util.Locale.getDefault().language == "zh"
            val exception = Exception(
                """
        ////// ${if (isChinese) it.title.chn else it.title.eng} /////
        ${if (isChinese) it.content.chn else it.content.eng}
        ${if (isChinese) "请将" else "Please replace"}
        ====>>
        //${conflictDartSource.absolutePath} 
        <<=== ${if (isChinese) "替换成" else "this source file with"}
        //${replaceFile.absolutePath} ${if (isChinese) "的内容" else " content"},
        
        ${if (isChinese) "然后执行如下命令生成新的源码快照" else "Then execute the following command to generate a new source snapshot"}
        
        ```
        rm ${flutterSdkPath}/bin/cache/flutter_tools.stamp & \
        rm ${flutterSdkPath}/bin/cache/flutter_tools.snapshot & \
        flutter pub get
        ```
        ${if (isChinese) it.description.chn else it.description.eng}
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
            } else if (textReplace != text && it.patchLocation == "flutter.groovy" && pluginsFile.exists()) {
                if (text.contains("@java.util.Optional")) { //说明已经修改了,但是被系统替换
                    //那么直接把conflictDartSource文件中的@java.util.Optional 替换成 @Optional
                    if (it.alert != null) {
                        println("warning:${if (isChinese) it.alert.chn else it.alert.eng}")
                    }
                    val textReplace2 =
                        conflictDartSource.readText().replace("@java.util.Optional", "@Optional")
                    conflictDartSource.writeText(textReplace2)

                    checkSource(conflictDartSource, it, replaceFile, flutterSdkPath)
                } else {
                    throw exception
                }
            }
        }

        fun setupFlutterPatch(flutterSdkPath: String) {
            patches.forEach {
                val replaceFile = file("patch/${it.patchLocation}") //${it.version} flutter版本号,查看了好多个版本的更新,发现并没有很大区别,所以这里不需要区分版本号
                val conflictDartSource =
                    file("${flutterSdkPath}/${it.sourceLocation}/${it.patchLocation}")
                if (!replaceFile.exists() && it.mustBeModified) {
                    throw Exception("${replaceFile.absolutePath} 文件是不能删除的!!!!")
                }
                if (conflictDartSource.exists()) {
                    checkSource(conflictDartSource, it, replaceFile, flutterSdkPath)
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