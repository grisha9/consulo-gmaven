package consulo.gmaven.settings;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.component.persist.StoragePathMacros;
import consulo.externalSystem.setting.AbstractExternalSystemLocalSettings;
import consulo.ide.ServiceManager;
import consulo.project.Project;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static consulo.gmaven.Constants.SYSTEM_ID;

@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
@Singleton
@State(name = "GMavenLocalSettings", storages = {@Storage(StoragePathMacros.WORKSPACE_FILE)})
public class LocalSettings extends AbstractExternalSystemLocalSettings
        implements PersistentStateComponent<AbstractExternalSystemLocalSettings.State> {

    @Inject
    protected LocalSettings(@NotNull Project project) {
        super(SYSTEM_ID, project);
    }

    @Nonnull
    public static LocalSettings getInstance(@Nonnull Project project) {
        return ServiceManager.getService(project, LocalSettings.class);
    }

    @Nullable
    @Override
    public State getState() {
        State state = new State();
        fillState(state);
        return state;
    }

    @Override
    public void loadState(@Nonnull State state) {
        super.loadState(state);
    }
}
