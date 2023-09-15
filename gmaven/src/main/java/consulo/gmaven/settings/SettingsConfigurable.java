package consulo.gmaven.settings;

import consulo.annotation.component.ExtensionImpl;
import consulo.configurable.ProjectConfigurable;
import consulo.externalSystem.service.execution.ExternalSystemSettingsControl;
import consulo.ide.impl.idea.openapi.externalSystem.service.settings.AbstractExternalSystemConfigurable;
import consulo.project.Project;
import jakarta.inject.Inject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static consulo.gmaven.Constants.SYSTEM_ID;

@ExtensionImpl
public class SettingsConfigurable extends
        AbstractExternalSystemConfigurable<MavenProjectSettings, MavenSettingsListener, MavenSettings>
        implements ProjectConfigurable {

    @Nonnull
    private final Project project;

    @Inject
    public SettingsConfigurable(@Nonnull Project project) {
        super(project, SYSTEM_ID);
        this.project = project;
    }

    @Nonnull
    @Override
    protected ExternalSystemSettingsControl<MavenProjectSettings> createProjectSettingsControl(
            @Nonnull MavenProjectSettings projectSettings
    ) {
        return new ProjectSettingsControl(project, projectSettings);
    }

    @Nullable
    @Override
    protected ExternalSystemSettingsControl<MavenSettings> createSystemSettingsControl(
            @Nonnull MavenSettings systemSettings
    ) {
        return new SystemSettingsControl(systemSettings);
    }

    @Nonnull
    @Override
    protected MavenProjectSettings newProjectSettings() {
        return new MavenProjectSettings();
    }

    @Nonnull
    @Override
    public String getId() {
        return "reference.settingsdialog.project.GMaven";
    }
}
