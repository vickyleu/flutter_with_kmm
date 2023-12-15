//需要判断是否是jitpack的构建，如果是jitpack的构建，需要将build目录设置到项目根目录下
if (System.getenv("JITPACK") == null) {
    rootProject.layout.buildDirectory.set(file("../build/buildSrc"))
}

plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}
dependencies {
    compileOnly("com.android.tools.build:gradle:8.2.0")
//    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.21")
}