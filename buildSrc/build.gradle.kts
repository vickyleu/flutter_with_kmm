//需要判断是否是jitpack的构建，如果是jitpack的构建，需要将build目录设置到项目根目录下
if (System.getenv("JITPACK") == null) {
    if(file("./android").exists()){
        rootProject.layout.buildDirectory.set(file("./build"))
//        rootProject.layout.buildDirectory.set(file("${rootProject.rootDir.parentFile.parentFile.absolutePath}/buildOut"))
    }else{
        rootProject.layout.buildDirectory.set(file("../build"))
    }
}
plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies{
    implementation(kotlin("stdlib"))
}