package consulo.gmaven.extensionpoints.plugin;

import consulo.annotation.component.ExtensionImpl;
import consulo.gmaven.api.model.MavenPlugin;
import consulo.gmaven.api.model.MavenProject;
import consulo.gmaven.extensionpoints.model.PluginContentRoots;
import consulo.gmaven.model.ProjectResolverContext;

import javax.annotation.Nonnull;

@ExtensionImpl
public class GroovyMavenPlugin implements MavenFullImportPlugin {

    @Nonnull
    @Override
    public String getGroupId() {
        return "org.codehaus.gmaven";
    }

    @Nonnull
    @Override
    public String getArtifactId() {
        return "groovy-maven-plugin";
    }

    @Nonnull
    @Override
    public PluginContentRoots getContentRoots(
            @Nonnull MavenProject mavenProject, @Nonnull MavenPlugin plugin, @Nonnull ProjectResolverContext context
    ) {
        return GroovyAbstractMavenPlugin.getContentRoots(mavenProject, plugin, context);
    }
}
