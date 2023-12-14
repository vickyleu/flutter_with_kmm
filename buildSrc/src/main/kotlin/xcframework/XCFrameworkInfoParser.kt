//package xcframework
//
//import groovy.util.Node
//import groovy.util.NodeList
//import groovy.xml.XmlParser
//import java.io.File
//
///**
// * Created by Sebastian Owodzin on 17/09/2022
// */
//class XCFrameworkInfoParser {
//
//    private val cache = mutableMapOf<String, List<LibraryDefinition>>()
//
//    fun parse(frameworkPath: File): List<LibraryDefinition> {
//        val infoPlistFile = frameworkPath.resolve("Info.plist")
//
//        return cache[infoPlistFile.absolutePath] ?: infoPlistFile.takeIf { it.exists() && it.isFile }?.let { file ->
//            try {
//                val infoPlist = XmlParser(false, false, true).parse(file)
//
//                (infoPlist.get("dict") as NodeList).getAt("array").getAt("dict").map {
//                    (it as Node).get("*") as NodeList
//                }.map { list ->
//                    val keys = mutableListOf<String>()
//                    val values = mutableListOf<List<String>>()
//
//                    list.mapIndexed { index, item ->
//                        val node = item as Node
//
//                        if (index % 2 == 0) {
//                            keys.add(node.text())
//                        } else {
//                            if (keys.last() == "SupportedArchitectures") {
//                                values.add((node.get("*") as NodeList).map { (it as Node).text() })
//                            } else {
//                                values.add(listOf(node.text()))
//                            }
//                        }
//                    }
//
//                    keys.mapIndexed { index: Int, s: String ->
//                        s to values[index]
//                    }.toMap()
//                }.map {
//                    LibraryDefinition(
//                        it["LibraryIdentifier"]!!.first(),
//                        it["LibraryPath"]!!.first(),
//                        it["SupportedArchitectures"]!!,
//                        it["SupportedPlatform"]!!.first(),
//                        it["SupportedPlatformVariant"]?.first()
//                    )
//                }.also {
//                    cache[infoPlistFile.absolutePath] = it
//                }
//            } catch (exception: Throwable) {
//                println("Error reading ${frameworkPath.path}/Info.plist, exception: $exception")
//                null
//            }
//        } ?: emptyList()
//    }
//}