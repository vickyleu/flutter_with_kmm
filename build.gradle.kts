import groovy.xml.XmlSlurper
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

//需要判断是否是jitpack的构建，如果是jitpack的构建，需要将build目录设置到项目根目录下
if (System.getenv("JITPACK") == null) {
    rootProject.layout.buildDirectory.set(file("./build"))
}
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false

    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.cocoapods) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.atomicfu) apply false
    alias(libs.plugins.sqldelight) apply false
    // 无法使用toml定义,flutter插件是动态引入的,编译时无法获取到
    id("dev.flutter.flutter-gradle-plugin") apply false
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
            } /*else if (requested.group == libs.kotlin.coroutines.get().group
                && requested.name == libs.kotlin.coroutines.get().name
            ) {
                useVersion(libs.versions.coroutines.get())
            }*/
        }
    }

    val androidCompileSdkInt = FlutterExtension.getCompileSdkVersion()
    val androidMinSdkInt = FlutterExtension.getMinSdkVersion()
    val androidMinSdkMinimal = 21

    this.afterEvaluate {
        val javaVersion = libs.versions.jdk.get()
        val kotlinVersion = libs.versions.kotlin.get()
        this.ext["kotlin_version"] = kotlinVersion
        val java = JavaVersion.valueOf("VERSION_$javaVersion")
        if (this.name != "shared" && this.name != "android") {
            if (this.hasProperty("android") && this.name != "gradle") {
                val androidProperty = this.property("android")
                if (androidProperty is com.android.build.gradle.LibraryExtension) {
                    if (androidProperty.namespace == null) {
                        androidProperty.sourceSets.getByName("main").manifest.srcFile.also {
                            val manifest = XmlSlurper().parse(file(it))
                            val packageName = manifest.getProperty("@package").toString()
                            androidProperty.namespace = packageName
                        }
                    }
                    androidProperty.buildFeatures.apply {
                        buildConfig = true
                    }
                    androidProperty.compileSdk = androidCompileSdkInt
                    var current = (androidProperty.defaultConfig.minSdk ?: androidMinSdkInt)
                    if (current < androidMinSdkMinimal) current = androidMinSdkMinimal
                    if (current < androidMinSdkInt) current = androidMinSdkInt
                    androidProperty.defaultConfig.minSdk = current

                    androidProperty.defaultConfig.targetSdk = FlutterExtension.getTargetSdkVersion()

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
                } else if (androidProperty is com.android.build.gradle.TestedExtension) {
                    if (androidProperty.namespace == null) {
                        androidProperty.sourceSets.getByName("main").manifest.srcFile.also {
                            val manifest = XmlSlurper().parse(file(it))
                            val packageName = manifest.getProperty("@package").toString()
                            println("Setting $packageName as android namespace")
                            androidProperty.namespace = packageName
                        }
                    }
                    androidProperty.buildFeatures.apply {
                        buildConfig = true
                    }
                    androidProperty.compileSdkVersion(androidCompileSdkInt)
                    var current = (androidProperty.defaultConfig.minSdk ?: androidMinSdkInt)
                    if (current < androidMinSdkMinimal) current = androidMinSdkMinimal
                    if (current < androidMinSdkInt) current = androidMinSdkInt
                    androidProperty.defaultConfig.minSdk = current

                    androidProperty.defaultConfig.targetSdk = FlutterExtension.getTargetSdkVersion()
                    androidProperty.ndkVersion = FlutterExtension.getNdkVersion()

                    androidProperty.compileOptions.sourceCompatibility = java
                    androidProperty.compileOptions.targetCompatibility = java
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
