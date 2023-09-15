package consulo.gmaven.wrapper;

import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.gmaven.MavenLog;
import consulo.project.Project;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

public final class MvnDotProperties {
    private static final String DISTRIBUTION_URL_PROPERTY = "distributionUrl";

    private MvnDotProperties() {
    }

    @Nonnull
    public static String getDistributionUrl(Project project, String projectPath) {
        var propertiesVFile = getWrapperPropertiesVFile(projectPath);
        if (propertiesVFile == null) return "";
        return getWrapperProperties(project, propertiesVFile).getProperty(DISTRIBUTION_URL_PROPERTY, "");
    }

    @Nonnull
    public  static String getJvmConfig(Path projectPath) {
        var jvmConfigVFile = getJvmConfigVFile(projectPath);
        if (jvmConfigVFile == null) return "";
        try {
            return new String(jvmConfigVFile.contentsToByteArray(true), jvmConfigVFile.getCharset());
        } catch (IOException e) {
            MavenLog.LOG.error(e);
            return "";
        }
    }

    private static VirtualFile getJvmConfigVFile(Path projectPath) {
        return Optional.ofNullable(LocalFileSystem.getInstance().findFileByNioFile(projectPath))
                .map(it -> it.findChild(".mvn"))
                .map(it -> it.findChild("jvm.config"))
                .orElse(null);
    }

    @Nullable
    private static VirtualFile getWrapperPropertiesVFile(String projectPath) {
        return Optional.ofNullable(LocalFileSystem.getInstance().findFileByPath(projectPath))
                .map(it -> it.findChild(".mvn"))
                .map(it -> it.findChild(".wrapper"))
                .map(it -> it.findChild("maven-wrapper.properties"))
                .orElse(null);

    }

    private static Properties getWrapperProperties(Project project, VirtualFile wrapperProperties) {
        return CachedValuesManager.getManager(project)
                .getCachedValue(
                        project,
                        () -> CachedValueProvider.Result.create(
                                getWrapperProperties(wrapperProperties), wrapperProperties
                        )
                );
    }

    @Nonnull
    private static Properties getWrapperProperties(VirtualFile wrapperProperties) {
        var properties = new Properties();
        try {
            properties.load(new ByteArrayInputStream(wrapperProperties.contentsToByteArray(true)));
        } catch (IOException e) {
            MavenLog.LOG.error(e);
        }
        return properties;
    }
}
