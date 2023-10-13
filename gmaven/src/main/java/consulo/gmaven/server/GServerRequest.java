package consulo.gmaven.server;

import consulo.content.bundle.Sdk;
import consulo.externalSystem.model.task.ExternalSystemTaskId;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import consulo.gmaven.settings.MavenExecutionSettings;

import java.nio.file.Path;

public class GServerRequest {
    public ExternalSystemTaskId taskId;
    public Path projectPath;
    public Path mavenPath;
    public Sdk sdk;
    public MavenExecutionSettings settings;
    public boolean installGMavenPlugin = false;
    public ExternalSystemTaskNotificationListener listener;

    public GServerRequest(ExternalSystemTaskId taskId,
                          Path projectPath,
                          Path mavenPath,
                          Sdk sdk,
                          MavenExecutionSettings settings,
                          ExternalSystemTaskNotificationListener listener) {
        this.taskId = taskId;
        this.projectPath = projectPath;
        this.mavenPath = mavenPath;
        this.sdk = sdk;
        this.settings = settings;
        this.listener = listener;
    }
}
