package consulo.gmaven.extensionpoints.plugin;

import consulo.externalSystem.rt.model.ExternalSystemSourceType;
import consulo.gmaven.api.model.MavenPlugin;
import consulo.gmaven.api.model.MavenProject;
import consulo.gmaven.api.model.PluginBody;
import consulo.gmaven.extensionpoints.model.MavenContentRoot;
import consulo.gmaven.extensionpoints.model.PluginContentRoots;
import consulo.gmaven.model.ProjectResolverContext;
import consulo.gmaven.util.MavenJDOMUtil;
import consulo.gmaven.util.MavenUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static consulo.gmaven.util.MavenJDOMUtil.JDOM_ELEMENT_EMPTY;

abstract class GroovyAbstractMavenPlugin {

    @Nonnull
    public static PluginContentRoots getContentRoots(
            @Nonnull MavenProject mavenProject, @Nonnull MavenPlugin plugin, @Nonnull ProjectResolverContext context
    ) {
        var executions = Optional.ofNullable(plugin.getBody()).map(PluginBody::getExecutions).orElse(List.of());
        var result = new ArrayList<MavenContentRoot>();
        var excluded = new HashSet<String>(4);

        String mainConfiguration = null;
        String testConfiguration = null;
        String generatedConfiguration = null;
        String generatedTestConfiguration = null;
        for (var execution : executions) {
            if (execution.goals.contains("compile")) {
                mainConfiguration = execution.configuration;
            }
            if (execution.goals.contains("testCompile") || execution.goals.contains("compileTests")) {
                testConfiguration = execution.configuration;
            }
            if (execution.goals.contains("generateStubs")) {
                generatedConfiguration = execution.configuration;
            }
            if (execution.goals.contains("generateTestStubs")) {
                generatedTestConfiguration = execution.configuration;
            }
        }
        getPathList(mavenProject, mainConfiguration, false, context)
                .forEach(it -> result.add(new MavenContentRoot(ExternalSystemSourceType.SOURCE, it)));
        getPathList(mavenProject, testConfiguration, true, context)
                .forEach(it -> result.add(new MavenContentRoot(ExternalSystemSourceType.TEST, it)));
        excluded.add(getExcludedPath(mavenProject, generatedConfiguration, context));
        excluded.add(getExcludedPath(mavenProject, generatedTestConfiguration, context));
        return new PluginContentRoots(result, excluded);
    }

    @Nonnull
    private static List<String> getPathList(
            @Nonnull MavenProject mavenProject,
            @Nullable String configuration,
            boolean isTest,
            @Nonnull ProjectResolverContext context
    ) {
        var element = MavenFullImportPlugin.parseConfiguration(configuration, context);
        if (element == JDOM_ELEMENT_EMPTY) {
            return getDefaultPath(mavenProject, isTest);
        }
        var dirs = MavenJDOMUtil.findChildrenValuesByPath(element, "sources", "fileset.directory");
        if (dirs.isEmpty()) {
            return getDefaultPath(mavenProject, isTest);
        }
        return dirs;
    }

    @Nonnull
    private static List<String> getDefaultPath(@Nonnull MavenProject mavenProject, boolean isTest) {
        var sourceFolderName = isTest ? "test" : "main";
        return List.of(Path.of(mavenProject.getBasedir(), "src", sourceFolderName, "groovy").toString());
    }

    @Nonnull
    private static String getExcludedPath(
            @Nonnull MavenProject mavenProject, @Nullable String configuration, @Nonnull ProjectResolverContext context
    ) {
        var element = MavenFullImportPlugin.parseConfiguration(configuration, context);
        if (element == JDOM_ELEMENT_EMPTY) return getDefaultExcludedDir(mavenProject);
        String outputDirectory = MavenJDOMUtil.findChildValueByPath(element, "outputDirectory", null);
        return outputDirectory != null ? outputDirectory : getDefaultExcludedDir(mavenProject);
    }

    @Nonnull
    private static String getDefaultExcludedDir(@Nonnull MavenProject mavenProject) {
        return MavenUtils.getGeneratedSourcesDirectory(mavenProject.getBuildDirectory(), false)
                .resolve("groovy-stubs").toString();
    }
}
