package consulo.gmaven.project.importProvider;

import com.intellij.java.language.LanguageLevel;
import consulo.application.Application;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkTable;
import consulo.externalSystem.service.execution.ExternalSystemSettingsControl;
import consulo.gmaven.settings.*;
import consulo.ide.impl.idea.openapi.externalSystem.service.settings.AbstractImportFromExternalSystemControl;
import consulo.java.execution.impl.util.JreSearchUtil;
import consulo.project.Project;
import consulo.project.ProjectManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static consulo.gmaven.Constants.SYSTEM_ID;

public class ImportMavenControl
        extends AbstractImportFromExternalSystemControl<MavenProjectSettings, MavenSettingsListener, MavenSettings> {

    private final Project defaultProject;

    public ImportMavenControl() {
        super(
                SYSTEM_ID,
                new MavenSettings(ProjectManager.getInstance().getDefaultProject()),
                getInitialProjectSettings()
        );
        defaultProject = ProjectManager.getInstance().getDefaultProject();
    }

    @Nonnull
    private static MavenProjectSettings getInitialProjectSettings() {
        MavenProjectSettings result = new MavenProjectSettings();
        Sdk sdk = JreSearchUtil.findSdkOfLevel(Application.get().getInstance(SdkTable.class), LanguageLevel.JDK_1_8, null);
        if (sdk != null) {
            result.setJdkName(sdk.getName());
        }
        return result;
    }

    @Nonnull
    @Override
    protected ExternalSystemSettingsControl<MavenProjectSettings> createProjectSettingsControl(
            @Nonnull MavenProjectSettings settings
    ) {
        ProjectSettingsControl settingsControl = new ProjectSettingsControl(defaultProject, settings);
        settingsControl.hideUseAutoImportBox();
        return settingsControl;
    }

    @Nullable
    @Override
    protected ExternalSystemSettingsControl<MavenSettings> createSystemSettingsControl(
            @Nonnull MavenSettings settings
    ) {
        return new SystemSettingsControl(settings);
    }

    @Override
    public void onLinkedProjectPathChange(@Nonnull String path) {

    }
}
