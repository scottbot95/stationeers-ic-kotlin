object Deps {
    object JetBrains {
        object Kotlin {
            private const val VERSION = "1.6.10"
            const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$VERSION"
            const val testCommon = "org.jetbrains.kotlin:kotlin-test-common:$VERSION"
            const val testJunit = "org.jetbrains.kotlin:kotlin-test-junit:$VERSION"
            const val testJs = "org.jetbrains.kotlin:kotlin-test-js:$VERSION"
            const val testAnnotationsCommon = "org.jetbrains.kotlin:kotlin-test-annotations-common:$VERSION"
        }
    }

    object Kotest {
        private const val VERSION = "5.1.0"
        const val gradlePlugin = "io.kotest:kotest-framework-multiplatform-plugin-gradle:$VERSION"
        const val kotestFrameworkEngine = "io.kotest:kotest-framework-engine:$VERSION"
        const val kotestAssertionsCore = "io.kotest:kotest-assertions-core:$VERSION"
        const val kotestJUnit5Runner = "io.kotest:kotest-runner-junit5-jvm:$VERSION"
    }

    object Microutils {
        object KotlinLogging {
            private const val VERSION = "2.1.21"
            const val kotlinLogging = "io.github.microutils:kotlin-logging:$VERSION"
        }
    }

    object Mockk {
        private const val VERSION = "1.12.3"
        const val mockkCommon = "io.mockk:mockk-common:$VERSION"
        const val mockk = "io.mockk:mockk:$VERSION"
    }

    object Pinterest {
        object Ktlint {
            private const val VERSION = "0.44"
            private const val PLUGIN_VERSION = "10.2.1"
            const val gradlePlugin = "org.jlleitschuh.gradle:ktlint-gradle:$PLUGIN_VERSION"
        }
    }

    object Slf4J {
        private const val VERSION = "1.7.36"
        const val slf4jSimple = "org.slf4j:slf4j-simple:$VERSION"
    }

    object Square {
        object Okio {
            private const val VERSION = "3.0.0"
            const val okio = "com.squareup.okio:okio:$VERSION"
            const val okioNodeFilesystem = "com.squareup.okio:okio-nodefilesystem:$VERSION"
            const val okioFakeFilesystem = "com.squareup.okio:okio-fakefilesystem:$VERSION"
        }
    }
}