import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import kotlin.io.path.readBytes

abstract class ProjectVersion : DefaultTask() {
    @TaskAction
    fun action() {
        try {
            val version =
                project.rootDir
                    .toPath()
                    .resolve("version")
                    .readBytes()
                    .toString(Charsets.UTF_8)
            logger.info("Read $version")
            project.version = version
        } catch (ex: Exception) {
            logger.warn("Unable to fetch version because of exception", ex)
        }
    }
}

class ProjectVersionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.tasks.register<ProjectVersion>("projectVersion")
    }
}
