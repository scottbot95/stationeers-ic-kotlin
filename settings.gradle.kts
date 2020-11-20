rootProject.name = "stationeers-ic-kotlin"
pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
    }
}
include("lib")
include("samples")

project(":lib").name = "stationeers-ic"
