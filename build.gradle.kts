allprojects {
    version = "2.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
        mavenLocal()
    }
}

configure(subprojects.filter { it.name == "samples" }) {
    configurations.all {
        resolutionStrategy.dependencySubstitution {
            substitute(module("com.github.scottbot95:stationeers-ic-core"))
                .using(project(":lib:core")).because("we work we unreleased development version")
        }
    }
}
