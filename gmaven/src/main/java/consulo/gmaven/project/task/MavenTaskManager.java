package consulo.gmaven.project.task;

import com.intellij.java.language.LanguageLevel;
import consulo.application.Application;
import consulo.content.bundle.SdkTable;
import consulo.externalSystem.model.task.ExternalSystemTaskId;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import consulo.externalSystem.rt.model.ExternalSystemException;
import consulo.externalSystem.task.ExternalSystemTaskManager;
import consulo.gmaven.server.GServerHelper;
import consulo.gmaven.server.GServerRemoteProcessSupport;
import consulo.gmaven.server.GServerRequest;
import consulo.gmaven.settings.MavenExecutionSettings;
import consulo.java.execution.impl.util.JreSearchUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static consulo.gmaven.util.MavenUtils.getMavenHome;

public class MavenTaskManager implements ExternalSystemTaskManager<MavenExecutionSettings> {
    private final Map<ExternalSystemTaskId, GServerRemoteProcessSupport> cancellationMap = new ConcurrentHashMap<>();

    @Override
    public void executeTasks(@Nonnull ExternalSystemTaskId id,
                             @Nonnull List<String> taskNames,
                             @Nonnull String projectPath,
                             @Nullable MavenExecutionSettings settings,
                             @Nonnull List<String> vmOptions,
                             @Nonnull List<String> scriptParameters,
                             @Nullable String debuggerSetup,
                             @Nonnull ExternalSystemTaskNotificationListener listener) throws ExternalSystemException {
        if (settings == null) throw new ExternalSystemException("settings is empty");
        var sdk = JreSearchUtil.findSdkOfLevel(
                Application.get().getInstance(SdkTable.class), LanguageLevel.JDK_1_8, settings.getJdkName()
        );
        var mavenHome = getMavenHome(settings.getDistributionSettings());
        try {
            var request = new GServerRequest(id, Path.of(projectPath), mavenHome, sdk, settings, listener);
            GServerHelper.runTasks(request, taskNames, process -> cancellationMap.put(id, process));
        } finally {
            cancellationMap.remove(id);
        }
    }

    @Override
    public boolean cancelTask(
            @Nonnull ExternalSystemTaskId externalSystemTaskId,
            @Nonnull ExternalSystemTaskNotificationListener listener) throws ExternalSystemException {
        GServerRemoteProcessSupport processSupport = cancellationMap.get(externalSystemTaskId);
        if (processSupport != null) processSupport.stopAll();
        return true;
    }
}
