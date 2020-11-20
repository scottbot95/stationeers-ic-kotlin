plugins {
    listOf(
        kotlin("jvm") version "1.4.20",
        kotlin("multiplatform") version "1.4.20",
        id("org.jlleitschuh.gradle.ktlint") version "9.4.1",
        id("org.jetbrains.dokka") version "1.4.10.2"
    ).forEach { it apply false}
}
