plugins {
    `multiplatform-setup`
}

apply<SnapshotPlugin>()

kotlin {
    sourceSets {
        named("commonMain") {
            dependencies {
                implementation(project(":lib:core"))
                implementation(Deps.Square.Okio.okio)
            }
        }

        named("commonTest") {
            dependencies {
                implementation(project(":lib:test-utils"))
            }
        }

        named("jsMain") {
            dependencies {
                implementation(Deps.Square.Okio.okioNodeFilesystem)
            }
        }
    }
}