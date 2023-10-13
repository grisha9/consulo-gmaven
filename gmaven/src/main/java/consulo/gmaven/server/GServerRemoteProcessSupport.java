package consulo.gmaven.server;

import consulo.content.bundle.Sdk;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.executor.Executor;
import consulo.externalSystem.model.task.ExternalSystemTaskId;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import consulo.gmaven.api.GMavenServer;
import consulo.gmaven.settings.MavenExecutionSettings;
import consulo.ide.impl.idea.execution.rmi.RemoteProcessSupport;
import consulo.process.ExecutionException;
import consulo.process.cmd.ParametersListUtil;
import consulo.process.event.ProcessEvent;
import consulo.util.dataholder.Key;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static consulo.gmaven.wrapper.MvnDotProperties.getJvmConfig;

public class GServerRemoteProcessSupport extends RemoteProcessSupport<Object, GMavenServer, Object> {
    private final ExternalSystemTaskId id;
    private final Sdk jdk;
    private final List<String> jvmConfigOptions;
    private final Path mavenPath;
    private final Path workingDirectory;
    private final ExternalSystemTaskNotificationListener systemTaskNotificationListener;
    private final MavenExecutionSettings executionSettings;

    public GServerRemoteProcessSupport(@Nonnull GServerRequest request) {
        super(GMavenServer.class);
        this.id = request.taskId;
        this.jdk = request.sdk;
        this.mavenPath = request.mavenPath;
        this.workingDirectory = request.projectPath.toFile().isDirectory()
                ? request.projectPath : request.projectPath.getParent();
        this.systemTaskNotificationListener = request.listener;
        this.executionSettings = request.settings;
        String jvmConfig = getJvmConfig(workingDirectory);
        this.jvmConfigOptions = StringUtil.isEmpty(jvmConfig)
                ? Collections.emptyList() : ParametersListUtil.parse(jvmConfig, true, true);
    }

    public ExternalSystemTaskId getId() {
        return id;
    }

    @Override
    protected void fireModificationCountChanged() {
    }

    @Override
    protected void logText(Object configuration, ProcessEvent event, Key outputType, Object info) {
        String text = StringUtil.notNullize(event.getText());
        if (true) {
            System.out.println(text);
        }
        if (systemTaskNotificationListener != null) {
            systemTaskNotificationListener.onTaskOutput(id, text, true);
        }
    }

    @Override
    protected String getName(Object o) {
        return getClass().getSimpleName();
    }

    @Override
    protected RunProfileState getRunProfileState(Object o, Object configuration, Executor executor) throws ExecutionException {
        return new MavenServerCmdState(jdk, mavenPath, workingDirectory, jvmConfigOptions, executionSettings);
    }
}
