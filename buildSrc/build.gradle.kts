plugins {
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(Deps.JetBrains.Kotlin.gradlePlugin)
    implementation(Deps.Kotest.gradlePlugin)
    implementation(Deps.Pinterest.Ktlint.gradlePlugin)
}

kotlin {
    // Add Deps to compilation, so it will become available in main project
    sourceSets.getByName("main").kotlin.srcDir("buildSrc/src/main/kotlin")
}
