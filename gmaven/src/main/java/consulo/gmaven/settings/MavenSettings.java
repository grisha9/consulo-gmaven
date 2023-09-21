package consulo.gmaven.settings;


import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.component.persist.StoragePathMacros;
import consulo.externalSystem.setting.AbstractExternalSystemSettings;
import consulo.externalSystem.setting.ExternalSystemSettingsListener;
import consulo.ide.ServiceManager;
import consulo.project.Project;
import consulo.util.io.FileUtil;
import consulo.util.xml.serializer.annotation.AbstractCollection;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;

@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
@Singleton
@State(name = "MavenSettings", storages = @Storage(StoragePathMacros.PROJECT_CONFIG_DIR + "/gmaven.xml"))
public class MavenSettings extends AbstractExternalSystemSettings<MavenSettings, MavenProjectSettings, MavenSettingsListener>
        implements PersistentStateComponent<MavenSettings.MyState> {

    private boolean offlineMode = false;
    private boolean skipTests = false;

    @Inject
    public MavenSettings(@Nonnull Project project) {
        super(MavenSettingsListener.TOPIC, project);
    }

    @Nonnull
    public static MavenSettings getInstance(@Nonnull Project project) {
        return ServiceManager.getService(project, MavenSettings.class);
    }


    @Override
    public void subscribe(@Nonnull ExternalSystemSettingsListener<MavenProjectSettings> listener) {
        getProject().getMessageBus().connect(getProject())
                .subscribe(MavenSettingsListener.TOPIC, new DelegatingSettingsListenerAdapter(listener));
    }

    @Override
    protected void copyExtraSettingsFrom(@Nonnull MavenSettings settings) {
        offlineMode = settings.offlineMode;
        skipTests = settings.skipTests;
    }

    @Nonnull
    @Override
    public MyState getState() {
        MyState state = new MyState();
        fillState(state);
        state.isOfflineMode = isOfflineMode();
        state.skipTests = isSkipTests();
        return state;
    }

    @Override
    public void loadState(@Nonnull MyState state) {
        super.loadState(state);
        setOfflineMode(state.isOfflineMode);
        setSkipTests(state.skipTests);
    }

    public boolean isOfflineMode() {
        return offlineMode;
    }

    public void setOfflineMode(boolean offlineMode) {
        this.offlineMode = offlineMode;
    }

    public boolean isSkipTests() {
        return skipTests;
    }

    public void setSkipTests(boolean skipTests) {
        this.skipTests = skipTests;
    }

    @Override
    public @Nullable MavenProjectSettings getLinkedProjectSettings(@Nonnull String projectPath) {
        Path projectAbsolutePath = Path.of(projectPath).toAbsolutePath();
        MavenProjectSettings projectSettings = super.getLinkedProjectSettings(projectPath);
        if (projectSettings == null) {
            for (MavenProjectSettings setting : getLinkedProjectsSettings()) {
                Path settingPath = Path.of(setting.getExternalProjectPath()).toAbsolutePath();
                if (FileUtil.isAncestor(settingPath.toFile(), projectAbsolutePath.toFile(), false)) {
                    return setting;
                }
            }
        }
        return projectSettings;
    }

    @Override
    protected void checkSettings(@Nonnull MavenProjectSettings projectSettings, @Nonnull MavenProjectSettings ps1) {

    }

    public static class MyState implements State<MavenProjectSettings> {
        private final Set<MavenProjectSettings> myProjectSettings = new TreeSet<>();
        public boolean isOfflineMode = false;
        public boolean skipTests = false;


        @Override
        @AbstractCollection(surroundWithTag = false, elementTypes = {MavenProjectSettings.class})
        public Set<MavenProjectSettings> getLinkedExternalProjectsSettings() {
            return myProjectSettings;
        }

        @Override
        public void setLinkedExternalProjectsSettings(Set<MavenProjectSettings> settings) {
            if (settings != null) {
                myProjectSettings.addAll(settings);
            }
        }
    }
}