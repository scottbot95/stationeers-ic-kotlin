plugins {
    `multiplatform-setup`
}

apply<SnapshotPlugin>()

kotlin {
    sourceSets {
        named("commonMain") {
            dependencies {
                implementation(Deps.Square.Okio.okio)
                implementation(Deps.JetBrains.Kotlinx.ImmutableCollections.immutableCollections)
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
