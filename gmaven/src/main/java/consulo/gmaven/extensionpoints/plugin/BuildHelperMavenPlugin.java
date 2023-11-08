package consulo.gmaven.extensionpoints.plugin;

import consulo.annotation.component.ExtensionImpl;
import consulo.gmaven.api.model.MavenPlugin;
import consulo.gmaven.api.model.MavenProject;
import consulo.gmaven.api.model.PluginBody;
import consulo.gmaven.api.model.PluginExecution;
import consulo.gmaven.extensionpoints.model.MavenContentRoot;
import consulo.gmaven.extensionpoints.model.PluginContentRoots;
import consulo.gmaven.model.ProjectResolverContext;
import consulo.util.lang.StringUtil;
import org.jdom.Element;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static consulo.externalSystem.rt.model.ExternalSystemSourceType.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

@ExtensionImpl
public class BuildHelperMavenPlugin implements MavenFullImportPlugin {

    @Nonnull
    @Override
    public String getGroupId() {
        return "org.codehaus.mojo";
    }

    @Nonnull
    @Override
    public String getArtifactId() {
        return "build-helper-maven-plugin";
    }

    @Nonnull
    @Override
    public PluginContentRoots getContentRoots(
            @Nonnull MavenProject mavenProject, @Nonnull MavenPlugin plugin, @Nonnull ProjectResolverContext context
    ) {
        var executions = Optional.ofNullable(plugin.getBody()).map(PluginBody::getExecutions).orElse(emptyList());
        var result = new ArrayList<MavenContentRoot>();
        for (var execution : executions) {
            if (execution.goals.contains("add-source")) {
                getPathList(execution, mavenProject, "sources", context)
                        .forEach(it -> result.add(new MavenContentRoot(SOURCE, it)));
            }
            if (execution.goals.contains("add-test-source")) {
                getPathList(execution, mavenProject, "sources", context)
                        .forEach(it -> result.add(new MavenContentRoot(TEST, it)));
            }
            if (execution.goals.contains("add-resource")) {
                getPathList(execution, mavenProject, "resources", context)
                        .forEach(it -> result.add(new MavenContentRoot(RESOURCE, it)));
            }
            if (execution.goals.contains("add-test-resource")) {
                getPathList(execution, mavenProject, "resources", context)
                        .forEach(it -> result.add(new MavenContentRoot(TEST_RESOURCE, it)));
            }
        }
        return new PluginContentRoots(result, emptySet());
    }

    @Nonnull
    private List<String> getPathList(
            @Nonnull PluginExecution execution,
            @Nonnull MavenProject mavenProject,
            @Nonnull String paramName,
            @Nonnull ProjectResolverContext context
    ) {
        var element = MavenFullImportPlugin.parseConfiguration(execution.configuration, context);
        var paths = new ArrayList<String>();
        List<Element> children = Optional.ofNullable(element.getChild(paramName)).map(Element::getChildren)
                .orElse(emptyList());
        for (var sourceElement : children) {
            var sourcePath = sourceElement.getTextTrim();
            if (StringUtil.isNotEmpty(sourcePath)) {
                paths.add(MavenFullImportPlugin.getAbsoluteContentPath(sourcePath, mavenProject));
            }
        }
        return paths;
    }
}

