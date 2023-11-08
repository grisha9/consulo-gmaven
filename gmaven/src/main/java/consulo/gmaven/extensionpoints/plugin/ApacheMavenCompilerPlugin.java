package consulo.gmaven.extensionpoints.plugin;

import com.intellij.java.language.LanguageLevel;
import consulo.annotation.component.ExtensionImpl;
import consulo.gmaven.MavenLog;
import consulo.gmaven.api.model.*;
import consulo.gmaven.extensionpoints.model.PluginContentRoots;
import consulo.gmaven.model.ProjectResolverContext;
import consulo.gmaven.util.MavenArtifactUtil;
import consulo.gmaven.util.MavenJDOMUtil;
import consulo.util.lang.StringUtil;
import org.jdom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.*;

import static com.intellij.java.language.LanguageLevel.HIGHEST;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNullElse;

@ExtensionImpl
public class ApacheMavenCompilerPlugin implements MavenCompilerFullImportPlugin {

    @Nullable
    @Override
    public String getAnnotationProcessorTagName() {
        return "annotationProcessorPaths";
    }

    @Nonnull
    @Override
    public String getGroupId() {
        return "org.apache.maven.plugins";
    }

    @Nonnull
    @Override
    public String getArtifactId() {
        return "maven-compiler-plugin";
    }

    @Nonnull
    @Override
    public PluginContentRoots getContentRoots(
            @Nonnull MavenProject mavenProject, @Nonnull MavenPlugin plugin, @Nonnull ProjectResolverContext context
    ) {
        List<MavenArtifact> dependencies = Objects.requireNonNullElse(plugin.getBody().getDependencies(), emptyList());
        var groovyDependency = dependencies.stream()
                .filter(it -> it.getGroupId().equals("org.codehaus.groovy"))
                .filter(it -> it.getArtifactId().equals("groovy-eclipse-compiler"))
                .findFirst();


        if (groovyDependency.isEmpty()) {
            new PluginContentRoots(Collections.emptyList(), Collections.emptySet());
        }
        return GroovyAbstractMavenPlugin.getContentRoots(mavenProject, plugin, context);
    }

    @Override
    public @Nonnull CompilerData getCompilerData(
            @Nonnull MavenProject project,
            @Nonnull MavenPlugin plugin,
            @Nonnull Path localRepositoryPath,
            @Nonnull Map<String, Element> contextElementMap) {
        var compilerProp = getCompilerProp(plugin.getBody(), project, contextElementMap);
        return toCompilerData(compilerProp, project, plugin, localRepositoryPath, contextElementMap);
    }

    @Nonnull
    private CompilerProp getCompilerProp(
            @Nonnull PluginBody body,
            @Nonnull MavenProject project,
            @Nonnull Map<String, Element> contextElementMap
    ) {
        var executions = body.getExecutions()
                .stream()
                .filter(it -> it.phase != null && (it.phase.equals("compile") || it.phase.equals("test-compile")))
                .toList();

        var compilerProp = executions.isEmpty()
                ? getCompilerProp(body.getConfiguration(), contextElementMap)
                : compilerProp(executions, contextElementMap);
        fillCompilerPropFromMavenProjectProperies(compilerProp, project);
        return compilerProp;
    }

    @Nonnull
    private CompilerProp getCompilerProp(@Nullable String configuration,
                                         @Nonnull Map<String, Element> contextElementMap) {
        if (configuration == null) {
            return new CompilerProp();
        }
        var element = getElement(configuration, contextElementMap);

        return new CompilerProp(
                getLanguageLevel(element.getChildTextTrim("release")),
                getLanguageLevel(element.getChildTextTrim("source")),
                getLanguageLevel(element.getChildTextTrim("target")),
                getLanguageLevel(element.getChildTextTrim("testRelease")),
                getLanguageLevel(element.getChildTextTrim("testSource")),
                getLanguageLevel(element.getChildTextTrim("testTarget"))
        );
    }

    private CompilerProp compilerProp(@Nonnull List<PluginExecution> executions,
                                      @Nonnull Map<String, Element> contextElementMap) {
        return executions.stream()
                .map(it -> getCompilerProp(it.configuration, contextElementMap))
                .reduce(this::sumCompilerProp)
                .orElseThrow();
    }

    private CompilerProp sumCompilerProp(@Nonnull CompilerProp acc, @Nonnull CompilerProp next) {
        acc.release = maxLanguageLevel(acc.release, next.release);
        acc.source = maxLanguageLevel(acc.source, next.source);
        acc.target = maxLanguageLevel(acc.target, next.target);
        acc.testRelease = maxLanguageLevel(acc.testRelease, next.testRelease);
        acc.testSource = maxLanguageLevel(acc.testSource, next.testSource);
        acc.testTarget = maxLanguageLevel(acc.testTarget, next.testTarget);
        return acc;
    }


    @Nullable
    private static LanguageLevel getLanguageLevel(@Nullable Object value) {
        return (value instanceof String) ? LanguageLevel.parse((String) value) : null;
    }

    @Nullable
    private LanguageLevel maxLanguageLevel(@Nullable LanguageLevel first, @Nullable LanguageLevel second) {
        if (first == null) return second;
        if (second == null) return first;
        return (first.isAtLeast(second)) ? first : second;
    }

    @Nonnull
    private Element getElement(@Nonnull String body, @Nonnull Map<String, Element> contextElementMap) {
        Element element = contextElementMap.get(body);
        if (element == null) {
            element = MavenJDOMUtil.parseConfiguration(body);
            contextElementMap.put(body, element);
        }
        return element;
    }

    private static void fillCompilerPropFromMavenProjectProperies(
            @Nonnull CompilerProp compilerProp,
            @Nonnull MavenProject project
    ) {
        if (compilerProp.release == null) {
            compilerProp.release = getLanguageLevel(project.getProperties().get("maven.compiler.release"));
        }
        if (compilerProp.source == null) {
            compilerProp.source = getLanguageLevel(project.getProperties().get("maven.compiler.source"));
        }
        if (compilerProp.target == null) {
            compilerProp.target = getLanguageLevel(project.getProperties().get("maven.compiler.target"));
        }
        if (compilerProp.testRelease == null) {
            compilerProp.testRelease = getLanguageLevel(project.getProperties().get("maven.compiler.testRelease"));
        }
        if (compilerProp.testSource == null) {
            compilerProp.testSource = getLanguageLevel(project.getProperties().get("maven.compiler.testSource"));
        }
        if (compilerProp.testTarget == null) {
            compilerProp.testTarget = getLanguageLevel(project.getProperties().get("maven.compiler.testTarget"));
        }
    }

    private CompilerData toCompilerData(
            @Nonnull CompilerProp compilerProp,
            @Nonnull MavenProject mavenProject,
            @Nonnull MavenPlugin plugin,
            @Nonnull Path localRepositoryPath,
            @Nonnull Map<String, Element> contextElementMap
    ) {
        var isReleaseEnabled = StringUtil.compareVersionNumbers(plugin.getVersion(), "3.6") >= 0;
        LanguageLevel source = null;
        LanguageLevel target = null;
        LanguageLevel testSource = null;
        LanguageLevel testTarget = null;
        if (isReleaseEnabled) {
            source = compilerProp.release;
            target = compilerProp.release;
            testSource = compilerProp.testRelease;
            testTarget = compilerProp.testRelease;
        }
        if (source == null) {
            source = compilerProp.source;
        }
        if (target == null) {
            target = compilerProp.target;
        }
        if (source == null || target == null) {
            var defaultCompilerData = getDefaultCompilerData(plugin, localRepositoryPath);
            if (source == null) {
                source = defaultCompilerData.getSourceLevel();
            }
            if (target == null) {
                target = defaultCompilerData.getTargetLevel();
            }
        }

        if (testSource == null) {
            testSource = requireNonNullElse(compilerProp.testSource, source);
        }
        if (testTarget == null) {
            testTarget = requireNonNullElse(compilerProp.testTarget, target);
        }

        var configurationElement = Optional.ofNullable(plugin.getBody().getConfiguration())
                .map(it -> getElement(it, contextElementMap))
                .orElse(null);
        var compilerArgs = MavenCompilerImporterUtils.collectCompilerArgs(mavenProject, configurationElement);
        return new CompilerData(
                source, target, testSource, testTarget, plugin.getBody().getAnnotationProcessorPaths(), compilerArgs
        );
    }

    @Nonnull
    private CompilerData getDefaultCompilerData(@Nonnull MavenPlugin plugin, @Nonnull Path localRepositoryPath) {
        var descriptor = MavenArtifactUtil.readPluginDescriptor(localRepositoryPath, plugin);
        if (descriptor == null) {
            MavenLog.LOG.warn("null descriptor $plugin");
            return new CompilerData(HIGHEST, emptyList(), emptyList());
        }
        var source = requireNonNullElse(LanguageLevel.parse(descriptor.getMyParams().get("source")), HIGHEST);
        var target = requireNonNullElse(LanguageLevel.parse(descriptor.getMyParams().get("target")), HIGHEST);
        return new CompilerData(source, target, source, target, emptyList(), emptyList());
    }

    private static class CompilerProp {
        @Nullable
        LanguageLevel release;
        @Nullable
        LanguageLevel source;
        @Nullable
        LanguageLevel target;
        @Nullable
        LanguageLevel testRelease;
        @Nullable
        LanguageLevel testSource;
        @Nullable
        LanguageLevel testTarget;

        public CompilerProp() {

        }

        public CompilerProp(@Nullable LanguageLevel release,
                            @Nullable LanguageLevel source,
                            @Nullable LanguageLevel target,
                            @Nullable LanguageLevel testRelease,
                            @Nullable LanguageLevel testSource,
                            @Nullable LanguageLevel testTarget) {
            this.release = release;
            this.source = source;
            this.target = target;
            this.testRelease = testRelease;
            this.testSource = testSource;
            this.testTarget = testTarget;
        }
    }

    @Nonnull
    public static CompilerData getDefaultCompilerData(
            @Nonnull MavenProject mavenProject, @Nonnull LanguageLevel defaultLanguageLevel
    ) {
        var compilerProp = new CompilerProp();
        fillCompilerPropFromMavenProjectProperies(compilerProp, mavenProject);
        LanguageLevel source = compilerProp.release;
        LanguageLevel target = compilerProp.release;
        LanguageLevel testSource = compilerProp.testRelease;
        LanguageLevel testTarget = compilerProp.testRelease;

        if (source == null) {
            source = compilerProp.source;
        }
        if (target == null) {
            target = compilerProp.target;
        }
        if (testSource == null) {
            testSource = compilerProp.testSource != null ? compilerProp.testSource : source;
        }
        if (testTarget == null) {
            testTarget = compilerProp.testTarget != null ? compilerProp.testTarget : target;
        }
        if (source == null || target == null) {
            return new CompilerData(defaultLanguageLevel, emptyList(), emptyList());
        }
        return new CompilerData(source, target, testSource, testTarget, emptyList(), emptyList());
    }
}
