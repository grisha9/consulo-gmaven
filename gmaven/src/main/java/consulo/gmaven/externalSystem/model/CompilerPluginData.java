package consulo.gmaven.externalSystem.model;

import consulo.externalSystem.model.Key;
import consulo.externalSystem.model.ProjectSystemId;
import consulo.externalSystem.service.project.AbstractExternalEntityData;

import javax.annotation.Nonnull;
import java.util.Collection;

import static consulo.externalSystem.util.ExternalSystemConstants.UNORDERED;

public class CompilerPluginData extends AbstractExternalEntityData {
    @Nonnull
    public static final Key<CompilerPluginData> KEY = Key.create(CompilerPluginData.class, UNORDERED);

    @Nonnull
    private final Collection<String> paths;
    @Nonnull
    private final Collection<String> arguments;
    @Nonnull
    private final String buildDirectory;
    @Nonnull
    private final String baseDirectory;

    public CompilerPluginData(@Nonnull ProjectSystemId owner,
                              @Nonnull Collection<String> paths,
                              @Nonnull Collection<String> arguments,
                              @Nonnull String buildDirectory,
                              @Nonnull String baseDirectory) {
        super(owner);
        this.paths = paths;
        this.arguments = arguments;
        this.buildDirectory = buildDirectory;
        this.baseDirectory = baseDirectory;
    }

    @Nonnull
    public Collection<String> getPaths() {
        return paths;
    }

    @Nonnull
    public Collection<String> getArguments() {
        return arguments;
    }

    @Nonnull
    public String getBuildDirectory() {
        return buildDirectory;
    }

    @Nonnull
    public String getBaseDirectory() {
        return baseDirectory;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if (!paths.equals(((CompilerPluginData)o).paths)) return false;
        if (!arguments.equals(((CompilerPluginData)o).arguments)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        var result = paths.hashCode();
        result = 31 * result + arguments.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CompilerPluginData{" +
                "paths=" + paths +
                ", arguments=" + arguments +
                ", buildDirectory='" + buildDirectory + '\'' +
                ", baseDirectory='" + baseDirectory + '\'' +
                '}';
    }
}
