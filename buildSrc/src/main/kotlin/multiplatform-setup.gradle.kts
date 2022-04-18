import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("kotlin-multiplatform")
    id("io.kotest.multiplatform")
    id("org.jlleitschuh.gradle.ktlint")
//    kotlin("plugin.serialization")
}

kotlin {
    jvm()

    js(IR) {
        nodejs()
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }

        named("commonMain") {
            dependencies {
//                implementation(Deps.JetBrains.Kotlinx.Coroutines.coroutines)
                implementation(Deps.Microutils.KotlinLogging.kotlinLogging)
            }
        }

        named("commonTest") {
            dependencies {
                implementation(Deps.JetBrains.Kotlin.testCommon)
                implementation(Deps.JetBrains.Kotlin.testAnnotationsCommon)
                implementation(Deps.Kotest.kotestFrameworkEngine)
                implementation(Deps.Kotest.kotestAssertionsCore)
                implementation(Deps.Kotest.kotestProperty)
                implementation(Deps.Kotest.kotestDatset)
            }
        }

        named("jvmTest") {
            dependencies {
                implementation(Deps.JetBrains.Kotlin.testJunit)
                implementation(Deps.Kotest.kotestJUnit5Runner)
                implementation(Deps.Slf4J.slf4jSimple)
            }
        }

        named("jvmMain") {
            dependencies {
                implementation(Deps.Microutils.KotlinLogging.kotlinLoggingJvm)
            }
        }

        named("jsMain") {
            dependencies {
                implementation(Deps.Microutils.KotlinLogging.kotlinLoggingJs)
            }
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()

        systemProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug")
    }
}
