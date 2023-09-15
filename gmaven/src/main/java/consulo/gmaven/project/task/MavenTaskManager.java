package consulo.gmaven.project.task;

import consulo.externalSystem.model.task.ExternalSystemTaskId;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import consulo.externalSystem.rt.model.ExternalSystemException;
import consulo.externalSystem.task.ExternalSystemTaskManager;
import consulo.gmaven.settings.MavenExecutionSettings;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MavenTaskManager implements ExternalSystemTaskManager<MavenExecutionSettings> {

    @Override
    public void executeTasks(@Nonnull ExternalSystemTaskId id,
                             @Nonnull List<String> taskNames,
                             @Nonnull String projectPath,
                             @Nullable MavenExecutionSettings settings,
                             @Nonnull List<String> vmOptions,
                             @Nonnull List<String> scriptParameters,
                             @Nullable String debuggerSetup,
                             @Nonnull ExternalSystemTaskNotificationListener listener) throws ExternalSystemException {


    }

    @Override
    public boolean cancelTask(
            @Nonnull ExternalSystemTaskId externalSystemTaskId,
            @Nonnull ExternalSystemTaskNotificationListener listener) throws ExternalSystemException {
        return false;
    }
}
