plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()
    js(IR) {
        nodejs {
            testTask {
            }
        }
    }

    sourceSets {
        named("commonMain") {
            dependencies {
                implementation(Deps.Square.Okio.okio)
                implementation(Deps.Kotest.kotestAssertionsCore)
                implementation(Deps.Kotest.kotestFrameworkEngine)
                implementation(Deps.JetBrains.Kotlinx.Serialization.jsonSerializer)
            }
        }

        named("jsMain") {
            dependencies {
                implementation(Deps.Square.Okio.okioNodeFilesystem)
            }
        }
    }
}
