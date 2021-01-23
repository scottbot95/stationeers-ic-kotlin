plugins {
    kotlin("jvm")
}

repositories {
    maven {
        name = "GithubPackages"
        url = uri("https://maven.pkg.github.com/scottbot95/stationeers-ic-kotlin")
    }
    mavenLocal()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")

    // Uncomment for real implementation
    // implementation("com.github.scottbot95:stationeers-ic-jvm:0.1.0")

    // Only used for sample project
    implementation(project(":stationeers-ic"))
}

tasks {
    test {
        useJUnitPlatform()
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    register<com.github.scottbot95.stationeers.ic.gradle.ExportScriptTask>("export") {
        scriptName = "ElectricFurnaceControl"
    }
}
