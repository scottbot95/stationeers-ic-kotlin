plugins {
    // TODO theoretically we should be able to use the pure-jvm plugin here instead
    kotlin("multiplatform")
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
                implementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
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
