import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import java.io.File

// TODO Make this work with more than just jvm tests
@Suppress("UNCHECKED_CAST")
class SnapshotPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val snapshotsDir = File(target.projectDir, "src/jvmTest/snapshots")
        target.tasks.apply {

            withType<Test>().configureEach {
                systemProperty("snapshots.dir", snapshotsDir)
                systemProperty("snapshots.update", System.getProperty("snapshots.update"))
            }

            val jvmTest = getByName<Test>("jvmTest")

            register<Test>("updateSnapshots") {
                group = "verification"
                useJUnitPlatform()
                systemProperty("snapshots.dir", snapshotsDir)
                systemProperty("snapshots.update", true)

                testClassesDirs = jvmTest.testClassesDirs
                classpath = jvmTest.classpath
            }
        }
    }
}