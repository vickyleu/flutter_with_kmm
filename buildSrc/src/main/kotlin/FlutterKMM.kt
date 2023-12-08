import org.gradle.api.Project
import org.gradle.api.GradleException
import java.util.Properties
import java.io.File
import java.nio.file.Paths

fun Project.latestFlutterVersion(): String {
    val localProperties = Properties()
    val localPropertiesFile: File =if(rootProject.file("./android").exists()){
        rootProject.file("./android/local.properties")
    }else{
        rootProject.file("local.properties")
    }
    if (localPropertiesFile.exists()) {
        localPropertiesFile.reader(Charsets.UTF_8).use { reader ->
            localProperties.load(reader)
        }
    }
    val flutterRootPath = localProperties.getProperty("flutter.sdk")
        ?: throw GradleException("Flutter SDK not found. Define location with flutter.sdk in the local.properties file.")
    val flutterRoot = project.file(flutterRootPath)
    if (!flutterRoot.isDirectory) {
        throw GradleException("flutter.sdk must point to the Flutter SDK directory")
    }
    fun useLocalEngine(): Boolean {
        return project.hasProperty("local-engine-repo")
    }
    return if (useLocalEngine())
        "+" // Match any version since there's only one.
    else "1.0.0-" + Paths.get(flutterRoot.absolutePath, "bin", "internal", "engine.version")
        .toFile().readText().trim()
}