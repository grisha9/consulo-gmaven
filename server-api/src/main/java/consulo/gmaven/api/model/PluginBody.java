package consulo.gmaven.api.model;


import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;

public final class PluginBody implements Serializable {
    @Nonnull
    private final List<PluginExecution> executions;
    @Nonnull
    private final List<String> annotationProcessorPaths;
    private final List<MavenArtifact> dependencies;
    private final String configuration;

    public PluginBody(@Nonnull List<PluginExecution> executions,
                      @Nonnull List<String> annotationProcessorPaths,
                      List<MavenArtifact> dependencies,
                      String configuration) {
        this.executions = executions;
        this.annotationProcessorPaths = annotationProcessorPaths;
        this.dependencies = dependencies;
        this.configuration = configuration;
    }

    @Nonnull
    public List<PluginExecution> getExecutions() {
        return executions;
    }

    @Nonnull
    public List<String> getAnnotationProcessorPaths() {
        return annotationProcessorPaths;
    }

    public List<MavenArtifact> getDependencies() {
        return dependencies;
    }

    public String getConfiguration() {
        return configuration;
    }
}
