import groovy.xml.XmlSlurper
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

//需要判断是否是jitpack的构建，如果是jitpack的构建，需要将build目录设置到项目根目录下
if (System.getenv("JITPACK") == null) {
    rootProject.layout.buildDirectory.set(file("./build"))
}
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.cocoapods) apply false

    alias(libs.plugins.kotlin.atomicfu) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.sqldelight) apply false

    alias(libs.plugins.flutter.plugin) apply false
}

val javaVersion = JavaVersion.toVersion(libs.versions.jdk.get())
check(JavaVersion.current().isCompatibleWith(javaVersion)) {
    "This project needs to be run with Java ${javaVersion.getMajorVersion()} or higher (found: ${JavaVersion.current()})."
}


subprojects {
    if (this.hasProperty("test")) {
        this.property("test")?.apply {
            this.closureOf<Test> {
                exclude("**/*")
            }
        }
    }
    if (System.getenv("JITPACK") == null) {
            this.layout.buildDirectory.set(file("${rootProject.layout.buildDirectory.get().asFile.absolutePath}/${project.name}"))
    }
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == libs.kotlin.reflect.get().group) {
                useVersion(libs.versions.kotlin.get())
            } else if (this.requested.group == "io.flutter") {
                try {
                    val engineVersion = latestFlutterVersion()
                    useVersion(engineVersion)
                } catch (ignore: Exception) {
                    println("${ignore.message}}")
                }
            }
        }
    }

    val androidCompileSdkInt = FlutterExtension.getCompileSdkVersion()
    val androidMinSdkInt = FlutterExtension.getMinSdkVersion()
    val androidTargetSdkInt = FlutterExtension.getTargetSdkVersion()


    val androidCompileSdkMinimal = "androidCompileSdkMinimal"
    val androidMinSdkMinimal = "androidMinSdkMinimal"
    val androidTargetSdkMinimal = "androidTargetSdkMinimal"


    this.afterEvaluate {
        val sdkMinimalMap = mapOf(
            androidCompileSdkMinimal to libs.versions.androidCompileSdkMinimal.get().toInt(),
            androidMinSdkMinimal to libs.versions.androidMinSdkMinimal.get().toInt(),
            androidTargetSdkMinimal to libs.versions.androidTargetSdkMinimal.get().toInt()
        )
        val javaVersion = libs.versions.jdk.get()
        val kotlinVersion = libs.versions.kotlin.get()
        this.ext["kotlin_version"] = kotlinVersion
        val java = JavaVersion.valueOf("VERSION_$javaVersion")
        try {
            if (this.name != "shared" ) { //&& this.name != "android"
                if (this.hasProperty("android") && this.name != "gradle") {
                    val androidProperty = this.property("android")
                    if (androidProperty is com.android.build.gradle.LibraryExtension) {
                        val sourceSets = androidProperty.sourceSets.getByName("main")
                        sourceSets.java.srcDirs(sourceSets.java.srcDirs + file("src/main/kotlin") + file("src/main/java"))
                        if (androidProperty.namespace == null) {
                            sourceSets.manifest.srcFile.also {
                                val manifest = XmlSlurper().parse(file(it))
                                val packageName = manifest.getProperty("@package").toString()
                                androidProperty.namespace = packageName
                            }
                        }
                        androidProperty.buildFeatures.apply {
                            buildConfig = true
                        }
                        androidProperty.sourceSets.getByName("main").apply {
                            println("project Library:${this@subprojects.name} Setting sourceSets +++ ${this.java.srcDirs}")
                        }
                        var currentCompileSdk = (androidProperty.compileSdk ?: androidCompileSdkInt)
                        if (sdkMinimalMap.containsKey(androidCompileSdkMinimal)) {
                            val compileSdkMinimal = sdkMinimalMap[androidCompileSdkMinimal]!!
                            if (currentCompileSdk < compileSdkMinimal) currentCompileSdk =
                                compileSdkMinimal
                        }
                        if (currentCompileSdk < androidCompileSdkInt) currentCompileSdk =
                            androidCompileSdkInt
                        androidProperty.compileSdk = currentCompileSdk


                        var currentMinSdk =
                            (androidProperty.defaultConfig.minSdk ?: androidMinSdkInt)
                        if (sdkMinimalMap.containsKey(androidMinSdkMinimal)) {
                            val minSdkMinimal = sdkMinimalMap[androidMinSdkMinimal]!!
                            if (currentMinSdk < minSdkMinimal) currentMinSdk = minSdkMinimal
                        }
                        if (currentMinSdk < androidMinSdkInt) currentMinSdk = androidMinSdkInt
                        androidProperty.defaultConfig.minSdk = currentMinSdk

                        var currentTargetSdk =
                            (androidProperty.defaultConfig.targetSdk ?: androidTargetSdkInt)
                        if (sdkMinimalMap.containsKey(androidTargetSdkMinimal)) {
                            val targetSdkMinimal = sdkMinimalMap[androidTargetSdkMinimal]!!
                            if (currentTargetSdk < targetSdkMinimal) currentTargetSdk =
                                targetSdkMinimal
                        }
                        if (currentTargetSdk < androidTargetSdkInt) currentTargetSdk =
                            androidTargetSdkInt

                        androidProperty.defaultConfig.targetSdk = currentTargetSdk

                        androidProperty.compileOptions.sourceCompatibility = java
                        androidProperty.compileOptions.targetCompatibility = java
                        androidProperty.ndkVersion = FlutterExtension.getNdkVersion()

                        androidProperty.lint {
                            abortOnError = false
                            warningsAsErrors = false
                            baseline =
                                file("${rootProject.layout.buildDirectory.get().asFile.absolutePath}/lint-baseline.xml")
                            disable += arrayListOf(
                                "MissingTranslation",
                                "KotlinNullnessAnnotation",
                                "MissingClass",
                                "UnusedResources",
                                "UnusedAttribute",
                                "UnusedIds",
                                "UnusedResourcesConfiguration"
                            )
                        }


                    }
                    else if (androidProperty is com.android.build.gradle.TestedExtension) {
                        val sourceSets = androidProperty.sourceSets.getByName("main")
                        sourceSets.java.srcDirs(sourceSets.java.srcDirs + file("src/main/kotlin") + file("src/main/java"))
                        if (androidProperty.namespace == null) {
                            sourceSets.manifest.srcFile.also {
                                val manifest = XmlSlurper().parse(file(it))
                                val packageName = manifest.getProperty("@package").toString()
                                println("Setting $packageName as android namespace")
                                androidProperty.namespace = packageName
                            }
                        }
                        androidProperty.buildFeatures.apply {
                            buildConfig = true
                        }
                        androidProperty.sourceSets.getByName("main").apply {
                            println("project:${this@subprojects.name} Setting sourceSets +++ ${this.java.srcDirs}")
                        }
                        var currentCompileSdk =
                            (androidProperty.compileSdkVersion?.toIntOrNull()
                                ?: androidCompileSdkInt)
                        if (sdkMinimalMap.containsKey(androidCompileSdkMinimal)) {
                            val compileSdkMinimal = sdkMinimalMap[androidCompileSdkMinimal]!!
                            if (currentCompileSdk < compileSdkMinimal) currentCompileSdk =
                                compileSdkMinimal
                        }
                        if (currentCompileSdk < androidCompileSdkInt) currentCompileSdk =
                            androidCompileSdkInt
                        androidProperty.compileSdkVersion(currentCompileSdk)

                        var currentMinSdk =
                            (androidProperty.defaultConfig.minSdk ?: androidMinSdkInt)
                        if (sdkMinimalMap.containsKey(androidMinSdkMinimal)) {
                            val minSdkMinimal = sdkMinimalMap[androidMinSdkMinimal]!!
                            if (currentMinSdk < minSdkMinimal) currentMinSdk = minSdkMinimal
                        }
                        if (currentMinSdk < androidMinSdkInt) currentMinSdk = androidMinSdkInt

                        androidProperty.defaultConfig.minSdk = currentMinSdk

                        var currentTargetSdk =
                            (androidProperty.defaultConfig.targetSdk ?: androidTargetSdkInt)
                        if (sdkMinimalMap.containsKey(androidTargetSdkMinimal)) {
                            val targetSdkMinimal = sdkMinimalMap[androidTargetSdkMinimal]!!
                            if (currentTargetSdk < targetSdkMinimal) currentTargetSdk =
                                targetSdkMinimal
                        }
                        if (currentTargetSdk < androidTargetSdkInt) currentTargetSdk =
                            androidTargetSdkInt
                        androidProperty.defaultConfig.targetSdk = currentTargetSdk
                        androidProperty.ndkVersion = FlutterExtension.getNdkVersion()

                        androidProperty.compileOptions.sourceCompatibility = java
                        androidProperty.compileOptions.targetCompatibility = java
                    }
                    val flutterVersion = this.latestFlutterVersion()
                    this.dependencies{
                        // 判断是否包含flutter的依赖,如果不包含,则添加依赖
                        val project = this@subprojects
                        //noinspection UseTomlInstead
                        val dep = project.configurations.findByName("compileOnly")?.dependencies
                        if(dep!=null){
                            if(dep.find { it.group == "io.flutter" } == null){
                                project.dependencies.add("compileOnly",
                                    project.dependencies.create("io.flutter:flutter_embedding_debug:$flutterVersion"))
                            }else if(dep.find { it.group == "io.flutter" && it.version != flutterVersion } != null){
                                val depend = dep.find { it.group == "io.flutter" && it.version != flutterVersion }!!
                                // 如果flutter的版本不一致,则替换
                                project.dependencies.add("compileOnly",
                                    project.dependencies.create("${depend.group}:${depend.name}:$flutterVersion"))
                            }
                        }else{
                            project.dependencies.add("compileOnly",
                                project.dependencies.create("io.flutter:flutter_embedding_debug:$flutterVersion"))
                        }
                    }
                }
                if (this.hasProperty("kotlin") && this.name != "gradle") {
                    val kotlinProperty = this.property("kotlin")
                    if (kotlinProperty is org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension) {
                        kotlinProperty.jvmToolchain(javaVersion.toInt())
                        kotlinProperty.compilerOptions.jvmTarget.set(JvmTarget.valueOf("JVM_$javaVersion"))
                        kotlinProperty.compilerOptions.suppressWarnings = true
                    }
                }
                if (this.hasProperty("java") && this.name != "gradle") {
                    val javaProperty = this.property("java")
                    if (javaProperty is org.gradle.api.plugins.internal.DefaultJavaPluginExtension) {
                        javaProperty.toolchain.languageVersion =
                            JavaLanguageVersion.of(javaVersion.toInt())
                        javaProperty.apply {
                            this.sourceCompatibility = java
                            this.targetCompatibility = java
                        }
                    }
                }
            }
        } catch (ignore: Exception) {
        }
    }
}
subprojects {
    try {
        if (this.name != "shared") {
            this.evaluationDependsOn(":androidApp")
        }
    } catch (ignore: Throwable) {
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory.get().asFile)
}
