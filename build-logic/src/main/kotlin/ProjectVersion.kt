import org.gradle.api.Plugin
import org.gradle.api.Project
import kotlin.io.path.readBytes

class ProjectVersionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val version =
            project.rootDir
                .toPath()
                .resolve("version")
                .readBytes()
                .toString(Charsets.UTF_8)
        project.getLogger().info("Read $version")
        project.version = version
        project.setProperty("version", version)
    }
}
