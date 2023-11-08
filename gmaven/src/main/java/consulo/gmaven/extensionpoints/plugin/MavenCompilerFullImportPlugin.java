package consulo.gmaven.extensionpoints.plugin;

import consulo.gmaven.api.model.MavenPlugin;
import consulo.gmaven.api.model.MavenProject;
import org.jdom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Map;

public interface MavenCompilerFullImportPlugin extends MavenFullImportPlugin {

    @Nonnull
    CompilerData getCompilerData(@Nonnull MavenProject project,
                                 @Nonnull MavenPlugin plugin,
                                 @Nonnull Path localRepositoryPath,
                                 @Nonnull Map<String, Element> contextElementMap);

    @Nullable
    String getAnnotationProcessorTagName();

}
