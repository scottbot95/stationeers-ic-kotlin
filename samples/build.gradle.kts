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

    // Uncomment for real implementation
    // implementation("com.github.scottbot95:stationeers-ic-jvm:0.1.0")

    // Only used for sample project
    implementation(project(":stationeers-ic"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
