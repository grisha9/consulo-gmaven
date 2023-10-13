package consulo.gmaven.extensionpoints.plugin;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.component.extension.ExtensionPointName;
import consulo.gmaven.api.model.MavenPlugin;
import consulo.gmaven.api.model.MavenProject;
import consulo.gmaven.extensionpoints.model.PluginContentRoots;
import consulo.gmaven.model.ProjectResolverContext;
import consulo.gmaven.util.MavenJDOMUtil;
import org.jdom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

@ExtensionAPI(ComponentScope.APPLICATION)
public interface MavenFullImportPlugin {
    ExtensionPointName<MavenFullImportPlugin> EP_NAME = ExtensionPointName.create(MavenFullImportPlugin.class);

    @Nonnull
    String getGroupId();

    @Nonnull
    String getArtifactId();

    default String getKey() {
        return getGroupId() + ":" + getArtifactId();
    }

    default boolean isApplicable(@Nonnull MavenPlugin plugin) {
        return getArtifactId().equals(plugin.getArtifactId()) && getGroupId().equals(plugin.getGroupId());
    }

    @Nonnull
    default PluginContentRoots getContentRoots(@Nonnull MavenProject mavenProject,
                                               @Nonnull MavenPlugin plugin,
                                               @Nonnull ProjectResolverContext context) {
        return new PluginContentRoots(Collections.emptyList(), Collections.emptySet());
    }

    @Nonnull
    static Element parseConfiguration(@Nullable String configuration,
                                      @Nonnull ProjectResolverContext context) {
        if (configuration == null) return MavenJDOMUtil.JDOM_ELEMENT_EMPTY;
        Element element = context.contextElementMap.get(configuration);
        if (element != null) return element;
        element = MavenJDOMUtil.parseConfiguration(configuration);
        context.contextElementMap.put(configuration, element);
        return element;
    }

    @Nonnull
    static String getAbsoluteContentPath(@Nonnull String sourcePath, @Nonnull MavenProject mavenProject) {
        var path = Paths.get(sourcePath);
        if (path.isAbsolute()) {
            return sourcePath;
        } else {
            return Path.of(mavenProject.getBasedir(), sourcePath).toAbsolutePath().toString();
        }
    }
}

