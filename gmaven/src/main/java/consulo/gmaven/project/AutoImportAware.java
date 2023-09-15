package consulo.gmaven.project;

import consulo.externalSystem.ExternalSystemAutoImportAware;
import consulo.gmaven.settings.MavenProjectSettings;
import consulo.gmaven.settings.MavenSettings;
import consulo.gmaven.util.MavenUtils;
import consulo.project.Project;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;


public class AutoImportAware implements ExternalSystemAutoImportAware {
    @Nullable
    @Override
    public String getAffectedExternalProjectPath(@Nonnull String changedFileOrDirPath, @Nonnull Project project) {
        var changedPath = Path.of(changedFileOrDirPath);
        if (Files.isDirectory(changedPath)) return null;
        var fileSimpleName = changedPath.getFileName().toString();
        if (!MavenUtils.isPomFileName(fileSimpleName) && !MavenUtils.isPotentialPomFile(fileSimpleName)) return null;


        var systemSettings = MavenSettings.getInstance(project);
        var projectsSettings = systemSettings.getLinkedProjectsSettings();
        if (projectsSettings.isEmpty()) return null;

        MavenProjectSettings linkedProjectSettings = systemSettings.getLinkedProjectSettings(changedPath.getParent().toString());
        return linkedProjectSettings != null ? linkedProjectSettings.getExternalProjectPath() : null;
    }


}
