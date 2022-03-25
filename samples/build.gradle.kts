plugins {
    // TODO theoretically we should be able to use the pure-jvm plugin here instead
    kotlin("multiplatform")
    id("io.kotest.multiplatform")
}

kotlin {
    jvm()

    sourceSets {
        named("jvmMain") {
            dependencies {
                implementation("com.github.scottbot95:stationeers-ic-core:2.0.0-SNAPSHOT")
            }
        }
        named("jvmTest") {
            dependencies {
                implementation(Deps.Kotest.kotestFrameworkEngine)
                implementation(Deps.Kotest.kotestAssertionsCore)
                implementation(Deps.Kotest.kotestJUnit5Runner)
            }
        }
    }
}

tasks {
    withType<Test>().configureEach {
        useJUnitPlatform()
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}
