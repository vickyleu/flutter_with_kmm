//需要判断是否是jitpack的构建，如果是jitpack的构建，需要将build目录设置到项目根目录下
if (System.getenv("JITPACK") == null) {
    rootProject.layout.buildDirectory.set(file("${rootProject.rootDir.parentFile.parentFile.absolutePath}/buildOut"))
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
}

/**
 * rootProject.buildDir = '../build'
 * subprojects {
 *     project.buildDir = "${rootProject.buildDir}/${project.name}"
 * }
 * subprojects {
 *     project.evaluationDependsOn(':app')
 * }
 */


allprojects {
    if (System.getenv("JITPACK") == null) {
        this.layout.buildDirectory.set(file("${rootProject.layout.buildDirectory.get().asFile.absolutePath}/${project.name}"))
    }
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == libs.kotlin.reflect.get().group) {
                useVersion(libs.versions.kotlin.get())
            } /*else if (requested.group == libs.kotlin.coroutines.get().group
                && requested.name == libs.kotlin.coroutines.get().name
            ) {
                useVersion(libs.versions.coroutines.get())
            }*/
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory.get().asFile)
}