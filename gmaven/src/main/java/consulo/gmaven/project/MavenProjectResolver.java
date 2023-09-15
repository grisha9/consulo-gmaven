package consulo.gmaven.project;

import consulo.content.bundle.Sdk;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.model.project.ContentRootData;
import consulo.externalSystem.model.project.ModuleData;
import consulo.externalSystem.model.task.ExternalSystemTaskId;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import consulo.externalSystem.rt.model.ExternalSystemException;
import consulo.externalSystem.service.project.ExternalSystemProjectResolver;
import consulo.externalSystem.service.project.ProjectData;
import consulo.gmaven.settings.MavenExecutionSettings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static consulo.gmaven.Constants.SYSTEM_ID;

public class MavenProjectResolver implements ExternalSystemProjectResolver<MavenExecutionSettings> {
    @Nullable
    @Override
    public DataNode<ProjectData> resolveProjectInfo(
            @Nonnull ExternalSystemTaskId id,
            @Nonnull String projectPath,
            boolean isPreviewMode,
            @Nullable MavenExecutionSettings settings,
            @Nonnull ExternalSystemTaskNotificationListener listener)
            throws ExternalSystemException, IllegalArgumentException, IllegalStateException {
        if (settings == null) throw new ExternalSystemException("settings is empty");
        if (isPreviewMode) {
            return getPreviewProjectDataNode(settings, id, projectPath, null, listener);
        }
        return getPreviewProjectDataNode(settings, id, projectPath, null, listener);
    }

    @Override
    public boolean cancelTask(@Nonnull ExternalSystemTaskId externalSystemTaskId,
                              @Nonnull ExternalSystemTaskNotificationListener listener) {
        return false;
    }

    private DataNode<ProjectData> getPreviewProjectDataNode(
            MavenExecutionSettings settings,
            ExternalSystemTaskId id,
            String projectPath,
            Sdk sdk,
            ExternalSystemTaskNotificationListener listener
    ) {
        var projectDataNode = getPreviewProjectDataNode(projectPath);
        var distributionPath = settings.getDistributionSettings().getPath();
        if (sdk != null && distributionPath != null) {
            //var buildPath = Path.of(settings.getExecutionWorkspace().getProjectBuildFile()) ?:projectPath)
            //firstRun(GServerRequest(id, buildPath, distributionPath, sdk, settings, listener = listener))
        }
        return projectDataNode;
    }

    private DataNode<ProjectData> getPreviewProjectDataNode(
            String projectPath
    ) {
        var projectDirectory = getProjectDirectory(projectPath).toAbsolutePath().toString();
        var projectName = new File(projectDirectory).getName();
        var projectData = new ProjectData(SYSTEM_ID, projectName, projectDirectory, projectDirectory);
        var projectDataNode = new DataNode<>(ProjectKeys.PROJECT, projectData, null);

        projectDataNode
                .createChild(
                        ProjectKeys.MODULE,
                        new ModuleData(projectName, SYSTEM_ID, projectName, projectDirectory, projectDirectory)
                )
                .createChild(
                        ProjectKeys.CONTENT_ROOT,
                        new ContentRootData(SYSTEM_ID, projectDirectory)
                );
        return projectDataNode;
    }

    private Path getProjectDirectory(String projectPath) {
        var projectNioPath = Path.of(projectPath);
        return Files.isDirectory(projectNioPath) ? projectNioPath : projectNioPath.getParent();
    }
}