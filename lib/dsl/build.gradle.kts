plugins {
    id("multiplatform-setup")
}

kotlin {
    sourceSets {
        named("commonMain") {
            dependencies {
                api(project(":lib:core"))
            }
        }
    }
}
