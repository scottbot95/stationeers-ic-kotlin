plugins {
    id("multiplatform-setup")
}

kotlin {
    sourceSets {
        named("commonMain") {
            dependencies {
                implementation(Deps.Square.Okio.okio)
            }
        }

        named("jsMain") {
            dependencies {
                implementation(Deps.Square.Okio.okioNodeFilesystem)
            }
        }
    }
}
